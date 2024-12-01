# WalkApp
**WalkApp** je mobilna aplikacija za praćenje fizičkih aktivnosti, sa posebnim fokusom na brojanje koraka i prikaz statistika o pređenim koracima po danima. Aplikacija koristi senzor koraka telefona da bi pratila kretanje korisnika u realnom vremenu, prikazujući informacije kao što su ukupan broj koraka i pređena razdaljina.

## Funkcionalnosti aplikacije

- Prikaz trenutnog broja koraka i pređene razdaljine u realnom vremenu.
- Grafikon koji vizualizuje pređene korake po datumu.
- Čuvanje trenutnog broja koraka u Firebase bazu podataka.

## Korišćenje aplikacije

### Instalacija i pokretanje:
Potrebno je klonirati repozitorijum sa GitHub-a.
Otvorite projekt u Android Studiju i pokrenite ga na fizičkom uređaju (zbog tačnosti senzora).
Link za Android Studio: https://developer.android.com/studio?gad_source=1&gclid=CjwKCAiArva5BhBiEiwA-oTnXebND3uwqOzQUdjaP0jn8-r2KLkHT8W6xufisp6Pr9M06WCaBWh4YhoCxT4QAvD_BwE&gclsrc=aw.ds

### Dodeljivanje dozvola:
Zbog korišćenja**Step Counter**-a i **Step Detector**-a, aplikacija će tražiti dozvolu za praćenje fizičke aktivnosti (ACTIVITY_RECOGNITION). Dodeljivanje dozvole je potrebno za rad senzora.

### Praćenje aktivnosti:
Aplikacija će automatski početi sa brojanjem koraka čim je pokrenuta. Korisnik može pratiti svoj napredak na grafiku i resetovati brojač koraka dugim pritiskom na glavni ekran.

## Korisnički interfejs aplikacije

Aplikacija koristi Jetpack Compose za korisnički interfejs, sa sledećim glavnim elementima:

- Brojač koraka i udaljenosti - Glavni prikaz prikazuje trenutni broj koraka i pređenu distancu, koji se ažuriraju u realnom vremenu.
- Grafikon dnevnih aktivnosti - Prikazuje pređene korake za svaki dan. Koristi Firebase za čuvanje i preuzimanje podataka, a LazyRow za horizontalno prikazivanje grafikona. Ovaj grafikon omogućava korisnicima da jednostavno prate svoj napredak tokom vremena.

## Firebase integracija

Aplikacija koristi Firebase Firestore za skladištenje podataka o korisničkim aktivnostima. Svaki put kada korisnik sačuva aktivnost dugim pritiskom na ekran, aplikacija kreira zapis u Firestore-u koji sadrži:
* userId: ID korisnika (Firebase Authentication)
* steps: Ukupan broj koraka
* distance: Pređena razdaljina u kilometrima
* date: Datum kada su koraci pređeni

### Kod za čuvanje podataka izgleda ovako:
```kotlin
fun saveWalkData(stepCount: Int, distance: Float) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    val walkData = hashMapOf(
        "userId" to userId,
        "steps" to stepCount,
        "distance" to distance,
        "date" to currentDate
    )
    db.collection("walks")
        .add(walkData)
        .addOnSuccessListener {
            Log.d("Steps", "Steps saved successfully!")
        }
        .addOnFailureListener { e ->
            Log.e("Steps", "Error saving steps", e)
        }
}
```

## Prava pristupa (Permissions)
Aplikacija traži dozvolu za praćenje aktivnosti (ACTIVITY_RECOGNITION), koja je obavezna kako bi senzori koraka mogli da funkcionišu. Aplikacija automatski traži ovu dozvolu pri prvom pokretanju.

```kotlin
RequestPermission(permission = android.Manifest.permission.ACTIVITY_RECOGNITION) {
    permissionGranted = true
}
```

# Senzori u android uređajima

Većina Android uređaja ima ugrađene senzore koji mere pokret, orijentaciju i razne uslove u okruženju. Ovi senzori mogu pružiti sirove podatke sa visokim nivoom preciznosti i tačnosti, što je korisno ako želite da pratite trodimenzionalno kretanje uređaja, njegovu poziciju, ili promene u okruženju u blizini uređaja. Za pristup ovim senzorima, Android koristi klasu SensorManager, koja omogućava otkrivanje senzora na uređaju i pristup njihovim podacima. Na primer:

- Igra može pratiti očitavanja sa senzora gravitacije kako bi prepoznala složene gestove i pokrete korisnika, kao što su nagib, potres, rotacija ili zamah.
- Igra može koristiti SensorManager za pristup senzoru gravitacije i pratiti očitavanja kako bi prepoznala složene gestove i pokrete korisnika, kao što su nagib, tresenje, rotacija ili zamah.
- Aplikacija za vremensku prognozu može koristiti SensorManager za rad sa senzorima temperature i vlažnosti kako bi izračunala i prikazala tačku rose.
Putna aplikacija može koristiti SensorManager za očitavanja senzora geomagnetnog polja i akcelerometra radi prikazivanja pravca kompasa.

### Android platforma podržava tri široke kategorije senzora:
**Senzori pokreta**
Ovi senzori mere sile ubrzanja i rotacione sile duž tri ose. Ova kategorija uključuje:

* Akcelerometar
* Senzor gravitacije
* Žiroskop
* Senzor rotacionog vektora

**Senzori okruženja**
Ovi senzori mere različite parametre iz okruženja, kao što su temperatura vazduha, pritisak, osvetljenost i vlažnost. Ova kategorija uključuje:

* Barometar
* Fotometar
* Termometar

**Senzori pozicije**
Ovi senzori mere fizičku poziciju uređaja. Ova kategorija uključuje:

* Senzor orijentacije
* Magnetometar

Senzori kojima možemo pristupiti pomoću Android senzor framework-a mogu biti hardverski i softverski
- Hardverski senzori su fizičke komponente ugrađene u uređaj, koje direktno mere specifične osobine okruženja, kao što su ubrzanje, snaga geomagnetnog polja ili ugaona promena.
- Softverski senzori nisu fizički uređaji, ali oponašaju hardverske senzore. Oni dobijaju podatke kombinovanjem podataka sa jednog ili više hardverskih senzora i ponekad se nazivaju virtuelni senzori. Na primer, senzori linearne akceleracije i gravitacije spadaju u ovu grupu.

**Tabela 1.** Tipovi senzora podržani od strane Android platforme

| **Sensor**                  | **Tip**           | **Opis**                                                                                                                                 | **Primena**                           |
|-----------------------------|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|
| **TYPE_ACCELEROMETER**      | Hardware           | Meri silu ubrzanja u m/s² koja deluje na uređaj duž sve tri fizičke ose (x, y i z), uključujući silu gravitacije.                              | Detekcija pokreta (tresenje, nagib, itd.).|
| **TYPE_AMBIENT_TEMPERATURE**| Hardware           | Meri temperaturu okoline u stepenima Celzijusa (°C). Pogledajte napomenu ispod.                                                                | Praćenje temperature vazduha.             |
| **TYPE_GRAVITY**            | Software/Hardware | Meri silu gravitacije u m/s² koja deluje na uređaj duž sve tri fizičke ose (x, y, z).                                                          | Detekcija pokreta (tresenje, nagib, itd.).|
| **TYPE_GYROSCOPE**          | Hardware           | Meri brzinu rotacije uređaja u radijanima po sekundi (rad/s) oko svake od tri fizičke ose (x, y i z).                                           | Detekcija rotacije (okretanje, spin, itd.).|
| **TYPE_LIGHT**              | Hardware           | Meri nivo osvetljenja okoline (iluminacija) u luksima (lx).                                                                                    | Kontrola osvetljenja ekrana.              |
| **TYPE_LINEAR_ACCELERATION**| Software/Hardware | Meri silu ubrzanja u m/s² koja deluje na uređaj duž sve tri fizičke ose (x, y, z), isključujući silu gravitacije.                              | Praćenje ubrzanja duž jedne ose.          |
| **TYPE_MAGNETIC_FIELD**     | Hardware           | Meri geomagnetno polje okoline za sve tri fizičke ose (x, y, z) u mikroteslama (µT).                                                           | Kreiranje kompasa.                        |
| **TYPE_ORIENTATION**        | Software           | Meri stepen rotacije koji uređaj pravi oko svih fizičkih osa (x, y, z). Počev od API nivoa 3, možete dobiti matricu nagiba i matricu rotacije za uređaj koristeći senzor gravitacije i senzor geomagnetnog polja zajedno sa metodom `getRotationMatrix()`. | Određivanje položaja uređaja.             |
| **TYPE_PRESSURE**           | Hardware           | Meri pritisak vazduha u okruženju u hektopaskalima (hPa) ili milibarima (mbar).                                                                | Praćenje promena vazdušnog pritiska.      |
| **TYPE_PROXIMITY**          | Hardware           | Meri blizinu objekta u centimetrima (cm) u odnosu na ekran uređaja. Ovaj senzor se obično koristi za određivanje da li se uređaj drži blizu uha korisnika. | Pozicija telefona tokom poziva.          |
| **TYPE_RELATIVE_HUMIDITY**  | Hardware           | Meri relativnu vlažnost vazduha u okruženju u procentima (%).                                                                                  | Praćenje tačke rose, apsolutne i relativne vlažnosti. |
| **TYPE_ROTATION_VECTOR**    | Software or Hardware | Meri orijentaciju uređaja pružajući tri elementa vektora rotacije uređaja.                                                                     | Detekcija pokreta i rotacije.             |
| **TYPE_TEMPERATURE**        | Hardware           | Meri temperaturu uređaja u stepenima Celzijusa (°C). Ova implementacija senzora varira između uređaja, a ovaj senzor je zamenjen senzorom `TYPE_AMBIENT_TEMPERATURE` na API nivou 14. | Praćenje temperature.                     |
                                      
### Sensor Framework 
Možete pristupiti ovim senzorima i dobiti sirove podatke senzora koristeći Android senzorski okvir. Senzor framework je deo paketa android.hardware i uključuje sledeće klase i interfejse:

**SensorManager**
Ova klasa se koristi za kreiranje instance servisa senzora. Ona pruža različite metode za pristup i listanje senzora, registrovanje i odregistrovanje slušaoca događaja senzora, kao i za dobijanje informacija o orijentaciji. Takođe pruža više konstanti senzora koje se koriste za izveštavanje o tačnosti senzora, podešavanje brzine prikupljanja podataka i kalibraciju senzora.

**Sensor**
Ova klasa omogućava kreiranje instance određenog senzora. Pruža različite metode koje vam omogućavaju da odredite mogućnosti senzora.

**SensorEvent**
Sistem koristi ovu klasu za kreiranje objekta događaja senzora, koji pruža informacije o događaju senzora. Objekat događaja senzora uključuje sledeće informacije: sirove podatke senzora, tip senzora koji je generisao događaj, tačnost podataka i vremensku oznaku za događaj.

**SensorEventListener**
Ovaj interfejs omogućava kreiranje dve metode povratnog poziva koje primaju obaveštenja (događaje senzora) kada se vrednosti senzora promene ili kada se promeni tačnost senzora.

### Osnovni zadaci senzorskih API-ja
U tipičnoj aplikaciji, senzorski API-ji se koriste za obavljanje dva osnovna zadatka:

* **Identifikacija senzora i njihovih mogućnosti**
Identifikacija senzora i njihovih mogućnosti u runtime-u je korisna ako vaša aplikacija ima funkcije koje se oslanjaju na određene tipove senzora ili njihove mogućnosti. Na primer, možda ćete želeti da identifikujete sve senzore prisutne na uređaju i da onemogućite bilo koje funkcije aplikacije koje zavise od senzora koji nisu prisutni. Takođe, možete identifikovati sve senzore određenog tipa kako biste odabrali implementaciju senzora koja ima optimalne performanse za vašu aplikaciju.

* **Praćenje događaja senzora**
Praćenje događaja senzora omogućava vam dobijanje sirovih podataka senzora. Događaj senzora se javlja svaki put kada senzor detektuje promenu u parametrima koje meri. Događaj senzora pruža četiri ključne informacije: naziv senzora koji je pokrenuo događaj, vremensku oznaku događaja, tačnost događaja i sirove podatke senzora koji su pokrenuli događaj.

### Dostupnost senzora
Dostupnost senzora varira od uređaja do uređaja, ali može varirati i između različitih verzija Android platforme. Ovo je zato što su Android senzori postepeno uvodili tokom nekoliko izdanja platforme. Na primer:

Mnogi senzori su uvedeni u Android 1.5 (API nivo 3), ali neki nisu bili implementirani niti dostupni za upotrebu sve do Android 2.3 (API nivo 9).
Slično tome, nekoliko senzora je uvedeno u Android 2.3 (API nivo 9) i Android 4.0 (API nivo 14).
Dva senzora su zastarela i zamenjena novim, boljim senzorima.
Tabela 2 sumira dostupnost svakog senzora po verzijama platforme. Samo četiri platforme su navedene jer su one uključivale promene vezane za senzore. Senzori koji su označeni kao zastareli i dalje su dostupni na sledećim platformama (pod uslovom da je senzor prisutan na uređaju), što je u skladu sa politikom unazadne kompatibilnosti Androida.

**Tabela 2.** Dostupnost senzora po platformama

| **Sensor**                 | **Android 4.0** (API Level 14) | **Android 2.3** (API Level 9) | **Android 2.2** (API Level 8) | **Android 1.5** (API Level 3) |
|----------------------------|---------------------------------|--------------------------------|--------------------------------|--------------------------------|
| TYPE_ACCELEROMETER         | Da                              | Da                             | Da                             | Da                             |
| TYPE_AMBIENT_TEMPERATURE   | Da                              | n/a                            | n/a                            | n/a                            |
| TYPE_GRAVITY               | Da                              | Da                             | n/a                            | n/a                            |
| TYPE_GYROSCOPE             | Da                              | Da                             | n/a¹                           | n/a¹                           |
| TYPE_LIGHT                 | Da                              | Da                             | Da                             | Da                             |
| TYPE_LINEAR_ACCELERATION   | Da                              | Da                             | n/a                            | n/a                            |
| TYPE_MAGNETIC_FIELD        | Da                              | Da                             | Da                             | Da                             |
| TYPE_ORIENTATION           | Da²                             | Da²                            | Da²                            | Da                             |
| TYPE_PRESSURE              | Da                              | Da                             | n/a                            | n/a                            |
| TYPE_PROXIMITY             | Da                              | Da                             | Da                             | Da                             |
| TYPE_RELATIVE_HUMIDITY     | Da                              | Da                             | n/a                            | n/a                            |
| TYPE_ROTATION_VECTOR       | Da                              | Da                             | n/a                            | n/a                            |
| TYPE_TEMPERATURE           | Da²                             | Da²                            | n/a                            | n/a                            |

¹ Nije dostupno na svim uređajima sa Android 2.2 i starijim.  
² Ovaj senzor je zastareo i zamenjen boljim senzorima u API Level 14.


# Ključne komponente
Ova aplikacija koristi tri ključne komponente za rad sa senzorima Android uređaja:

**SensorManager** - za upravljanje senzorima.<br>
**Step Counter** - za praćenje ukupnog broja koraka od poslednjeg resetovanja.<br>
**Step Detector** - za otkrivanje pojedinačnih koraka u realnom vremenu.
**Accelerometer** - za merenje linearnog ubrzanja uređaja duž tri ose: x, y, z
**Gyroscope** - za merenje ugaone brzine rotacije oko njegovih osa: x, y, z

## SensorManager

Kao što je već pomenuto, **SensorManager** je klasa u Android SDK koja omogućava pristup senzorima dostupnim na uređaju, kao što su akcelerometar, žiroskop, barometar i drugi. Koristi se za upravljanje i rad sa senzorima, uključujući Step Counter i Step Detector. 


### Osnovni koraci za rad sa SensorManager-om:

**Instanciranje** <br>
Prvo, potrebno je dobiti instancu SensorManager preko konteksta aplikacije:

```kotlin
val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
```
**Pronalaženje senzora**<br>
Nakon toga, možemo pronaći željeni senzor koristeći:

```kotlin
val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
```

**Registracija senzora**<br> 
SensorManager omogućava registraciju senzorskih događaja, što znači da možemo slušati promene u vrednostima senzora. Registrujemo SensorEventListener koji će reagovati na promene u senzoru.

```kotlin
sensorManager.registerListener(stepListener, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
```
**Odjava senzora**<br>
Kada senzor više nije potreban (npr. kada korisnik zatvori aplikaciju), preporučuje se da odjavimo SensorEventListener kako bismo uštedeli bateriju:

```kotlin
sensorManager.unregisterListener(stepListener)
```
## Step Counter
Step Counter je vrsta senzora koji broji ukupne korake od trenutka kada je uređaj uključen ili kada je aplikacija resetovala podatke. On može da koristi akcelerometar uređaja za detekciju ciklusa hodanja (npr. kretanje gore-dole pri svakom koraku).

Kako radi: <br>
Step Counter vraća ukupan broj koraka kao relativnu vrednost, tj. od trenutka kada je senzor prvi put pokrenut.
Ova vrednost nije resetovana dok se uređaj ne restartuje ili senzor ne resetuje, što znači da aplikacija mora pratiti razliku između prethodno očitanih i trenutno očitanih koraka. <br>
Primer korišćenja u kodu:

```kotlin
val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
sensorManager.registerListener(object : SensorEventListener {
    override fun onSensorChanged(event: SensorEvent?) {
        val steps = event?.values?.get(0) ?: 0
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
```
## Step Detector
Step Detector je senzor koji detektuje pojedinačne korake korisnika. Za razliku od Step Counter-a koji daje ukupni broj koraka, Step Detector reaguje na svaki korak kao događaj, što ga čini korisnim za real-time aplikacije koje zahtevaju trenutne informacije o koraku.

Kako radi: <br>
Kada korisnik napravi korak, Step Detector generiše događaj koji se može iskoristiti za azuriranje interfejsa u realnom vremenu.
Ovaj senzor koristi algoritme za prepoznavanje uzorka koraka i može varirati u preciznosti zavisno od uređaja.<br>
Primer korišćenja u kodu:

```kotlin
val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
sensorManager.registerListener(object : SensorEventListener {
    override fun onSensorChanged(event: SensorEvent?) {
        val stepDetected = event != null
        if (stepDetected) {
            _stepCount.value += 1
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
```
## Prednosti i izazovi u korišćenju senzora za praćenje koraka

Korišćenje Step Counter i Step Detector senzora ima svoje prednosti i izazove:

**Prednosti:** Oba senzora su energetski efikasnija u poređenju sa korišćenjem sirovih podataka akcelerometra, jer su dizajnirana da procesiraju podatke direktno na hardveru. 

**Izazovi:** Preciznost može varirati između uređaja, a Step Detector može izazvati greške ako korisnik radi aktivnosti koje nisu hodanje. Takođe, Step Counter može biti resetovan pri restartovanju uređaja, pa je bitno voditi računa o tome kako se podaci skladište i ažuriraju.

## Akcelerometar (Accelerometer)
Akcelerometar je senzor koji meri ubrzanje uređaja duž tri ose: x, y i z. Ove vrednosti omogućavaju detekciju promena u brzini i pravcu kretanja uređaja.

Kako radi:
Akcelerometar meri linearno ubrzanje uređaja u m/s². Kada je uređaj u mirovanju, prikazuje ubrzanje zbog gravitacije (oko 9.8 m/s² na osi z).
Dinamičke promene (npr. nagli pad) se detektuju na osnovu promene magnitude ubrzanja.
Upotreba u aplikaciji: Akcelerometar se koristi za detekciju naglih promena ubrzanja koje mogu ukazivati na pad. <br>
Primer korišćenja u kodu:

```kotlin
Sensor.TYPE_ACCELEROMETER -> {
                            val x = event.values[0]
                            val y = event.values[1]
                            val z = event.values[2]
                            accelerationMagnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                        }
```
## Žiroskop (Gyroscope)
Žiroskop meri ugaone brzine rotacije uređaja oko njegovih osa: x, y i z. Za razliku od akcelerometra, koji meri linearno ubrzanje, žiroskop detektuje rotacione pokrete.

Kako radi:
Izmerene vrednosti su u radijanima po sekundi (rad/s).
Pomaže u identifikaciji rotacija koje često prate padove (npr. rotacija uređaja dok korisnik pada). <br>
Primer korišćenja u kodu:

```kotlin
Sensor.TYPE_GYROSCOPE -> {
                            val x = event.values[0]
                            val y = event.values[1]
                            val z = event.values[2]
                            angularVelocityMagnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                        }
```
## Prednosti i izazovi u upotrebi akcelerometra i žiroskopa
**Prednosti:**
Veća preciznost pri detekciji kompleksnih pokreta, poput pada.
Smanjen broj lažnih alarma zahvaljujući dodatnim informacijama iz žiroskopa.
Energetski efikasni u poređenju sa alternativnim metodama (npr. mašinsko učenje na sirovim podacima).
**Izazovi:**
Pragovi za ubrzanje i rotaciju moraju se fino podešavati na osnovu testiranja kako bi se izbegli lažni alarmi.
Različiti uređaji imaju senzore različitog kvaliteta, što može uticati na preciznost.

Akcelerometar i žiroskop su ključni senzori za aplikacije koje zahtevaju detekciju pada ili naprednu analizu kretanja. Njihova kombinovana upotreba omogućava preciznije rezultate u realnom vremenu, dok se istovremeno smanjuje broj lažnih alarma.
