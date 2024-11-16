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
*userId: ID korisnika (Firebase Authentication)
*steps: Ukupan broj koraka
*distance: Pređena razdaljina u kilometrima
*date: Datum kada su koraci pređeni

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

- Igra može pratiti očitavanja sa senzora gravitacije kako bi prepoznala složene gestove i pokrete korisnika, kao što su nagib, tresenje, rotacija ili zamah.
- Igra može koristiti SensorManager za pristup senzoru gravitacije i pratiti očitavanja kako bi prepoznala složene gestove i pokrete korisnika, kao što su nagib, tresenje, rotacija ili zamah.
- Aplikacija za vremensku prognozu može koristiti SensorManager za rad sa senzorima temperature i vlažnosti kako bi izračunala i prikazala tačku rose.
Putna aplikacija može koristiti SensorManager za očitavanja senzora geomagnetnog polja i akcelerometra radi prikazivanja pravca kompasa.

### Android platforma podržava tri široke kategorije senzora:
**Senzori pokreta**
Ovi senzori mere sile ubrzanja i rotacione sile duž tri ose. Ova kategorija uključuje:

*Akcelerometar
*Senzor gravitacije
*Žiroskop
*Senzor rotacionog vektora

**Senzori okruženja**
Ovi senzori mere različite parametre iz okruženja, kao što su temperatura vazduha, pritisak, osvetljenost i vlažnost. Ova kategorija uključuje:

*Barometar
*Fotometar
*Termometar

**Senzori pozicije**
Ovi senzori mere fizičku poziciju uređaja. Ova kategorija uključuje:

*Senzor orijentacije
*Magnetometar

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
                                      



Ova aplikacija koristi tri ključne komponente za rad sa senzorima Android uređaja:

SensorManager - za upravljanje senzorima.
Step Counter - za praćenje ukupnog broja koraka od poslednjeg resetovanja.
Step Detector - za otkrivanje pojedinačnih koraka u realnom vremenu.

# Ključne komponente: *Sensor Manager*, *Step Counter* i *Step Detector*
## SensorManager

**SensorManager** je klasa u Android SDK koja omogućava pristup senzorima dostupnim na uređaju, kao što su akcelerometar, žiroskop, barometar i drugi. Koristi se za upravljanje i rad sa senzorima, uključujući Step Counter i Step Detector. 


### Osnovni koraci za rad sa SensorManager-om:

**Instanciranje:** 
Prvo, potrebno je dobiti instancu SensorManager preko konteksta aplikacije:

```kotlin
val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
```
**Pronalaženje senzora:**
Nakon toga, možemo pronaći željeni senzor (u ovom slučaju Step Counter i Step Detector) koristeći:

```kotlin
val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
```

**Registracija senzora:** 
SensorManager omogućava registraciju senzorskih događaja, što znači da možemo slušati promene u vrednostima senzora. Registrujemo SensorEventListener koji će reagovati na promene u senzoru.

```kotlin
sensorManager.registerListener(stepListener, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
```
**Odjava senzora:**
Kada senzor više nije potreban (npr. kada korisnik zatvori aplikaciju), preporučuje se da odjavimo SensorEventListener kako bismo uštedeli bateriju:

```kotlin
sensorManager.unregisterListener(stepListener)
```
## Step Counter
Step Counter je vrsta senzora koji broji ukupne korake od trenutka kada je uređaj uključen ili kada je aplikacija resetovala podatke. On može da koristi akcelerometar uređaja za detekciju ciklusa hodanja (npr. kretanje gore-dole pri svakom koraku).

Kako radi:
Step Counter vraća ukupan broj koraka kao relativnu vrednost, tj. od trenutka kada je senzor prvi put pokrenut.
Ova vrednost nije resetovana dok se uređaj ne restartuje ili senzor ne resetuje, što znači da aplikacija mora pratiti razliku između prethodno očitanih i trenutno očitanih koraka.

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

Kako radi:
Kada korisnik napravi korak, Step Detector generiše događaj koji se može iskoristiti za azuriranje interfejsa u realnom vremenu.
Ovaj senzor koristi algoritme za prepoznavanje uzorka koraka i može varirati u preciznosti zavisno od uređaja.
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
