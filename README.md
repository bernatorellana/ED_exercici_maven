# Introducció a Maven

Al pràctic anterior vau compilar i executar el programa `generador-pdf` **a mà**: vau buscar i baixar un per un tots els `.jar` necessaris i vau escriure el *classpath* a les comandes `javac` i `java`. Funciona, però és pesat i fràgil: quins jars calen exactament? quina versió? i si una llibreria en necessita una altra?

En aquest document farem el mateix projecte amb **Apache Maven**, una eina de construcció (*build tool*) que s'encarrega de:

- **Gestionar les dependències**: declares quines llibreries vols i Maven les baixa, amb totes les que aquestes necessiten.
- **Estandarditzar l'estructura** del projecte: tots els projectes Maven tenen les carpetes al mateix lloc.
- **Automatitzar el cicle de construcció**: compilar, executar els tests, empaquetar... amb una sola comanda.

Anirem introduint els conceptes i aplicant-los pas a pas sobre el projecte. Les comandes marcades amb ▶ són les que heu d'executar.

> **Nota sobre les versions.** Aquest document s'ha provat amb Maven 3.10.0, JDK 17 com a versió objectiu, PDFBox 3.0.8 i Commons Text 1.15.0 (octubre de 2026). Si feu servir versions més noves, la majoria de sortides seran iguals o molt semblants.

---

## Índex

1. [Instal·lació de Maven a Windows 11](#1-installació-de-maven-a-windows-11)
2. [Creació del projecte](#2-creació-del-projecte)
3. [Estructura de carpetes d'un projecte Maven](#3-estructura-de-carpetes-dun-projecte-maven)
4. [L'arxiu POM](#4-larxiu-pom)
5. [El cicle de vida](#5-el-cicle-de-vida)
6. [Plugins i goals](#6-plugins-i-goals)
7. [Afegir dependències](#7-afegir-dependències)
8. [L'arbre de dependències](#8-larbre-de-dependències)
9. [Executar el programa amb dependències](#9-executar-el-programa-amb-dependències)
10. [Empaquetar amb una carpeta `lib`](#10-empaquetar-amb-una-carpeta-lib)
11. [Empaquetar en un *fat jar*](#11-empaquetar-en-un-fat-jar)
12. [Fer servir altres projectes locals propis](#12-fer-servir-altres-projectes-locals-propis)
13. [El Maven Wrapper](#13-el-maven-wrapper)
14. [Treballar amb NetBeans](#14-treballar-amb-netbeans)
15. [Resum de comandes](#15-resum-de-comandes)

---

## 1. Instal·lació de Maven a Windows 11

### 1.1 Prerequisit: un JDK

Maven és un programa Java i necessita un **JDK** (no n'hi ha prou amb un JRE). Comproveu-ho obrint un terminal (cmd o PowerShell):

▶
```
java -version
javac -version
```

Totes dues comandes han de respondre amb una versió. En aquest document compilem per a **Java 17**.

> ⚠️ **Verifiqueu la vostra versió.** Si el vostre JDK és més nou (21, 25...) no cal fer res: pot compilar per a Java 17. Si és més antic que el 17, o bé instal·leu-ne un de més nou, o bé caldrà canviar la propietat `maven.compiler.release` del POM (ho veurem a la [secció 4](#4-larxiu-pom)) pel número de la vostra versió.

Maven troba Java a través de la variable d'entorn `JAVA_HOME` o, si no existeix, a través del `PATH`. Si ho podeu, definiu `JAVA_HOME` apuntant a la carpeta del JDK (per exemple `C:\Program Files\Java\jdk-17`), sense `\bin` al final.

### 1.2 Descàrrega

1. Aneu a <https://maven.apache.org/download.cgi>.
2. Baixeu el **Binary zip archive**: `apache-maven-3.10.0-bin.zip` (o la versió 3.x que hi hagi en aquell moment).
   - No baixeu el *source* (codi font de Maven), ni la versió 4.x, que encara és una *release candidate*.
3. Descomprimiu-lo en una carpeta estable, per exemple `C:\eines\apache-maven-3.10.0`.
   Dins hi haureu de veure les carpetes `bin`, `boot`, `conf` i `lib`.

### 1.3 Afegir Maven al PATH

L'objectiu és que Windows trobi l'executable `mvn` (que és `bin\mvn.cmd`) des de qualsevol carpeta.

1. Premeu la tecla Windows i escriviu **variables d'entorn**. Obriu **Edita les variables d'entorn per al vostre compte** (no cal ser administrador).
2. A **Variables d'usuari**, seleccioneu `Path` → **Edita...**
3. **Nou** → escriviu el camí a la carpeta `bin` de Maven:
   `C:\eines\apache-maven-3.10.0\bin`
4. Accepteu totes les finestres.
5. **Tanqueu i torneu a obrir el terminal.** Els terminals ja oberts no veuen els canvis.

> Eviteu `setx PATH ...` per modificar el PATH: pot truncar-lo si és llarg.

### 1.4 Comprovació

▶
```
mvn -v
```

Ha de mostrar alguna cosa semblant a:

```
Apache Maven 3.10.0 (...)
Maven home: C:\eines\apache-maven-3.10.0
Java version: 17.0.x, vendor: ..., runtime: C:\Program Files\Java\jdk-17
...
```

Fixeu-vos que la línia `Java version` indiqui el JDK que esperàveu.

### 1.5 ⚠️ PowerShell i els paràmetres `-D`

El terminal per defecte de Windows 11 és **PowerShell**. Maven rep molts paràmetres de la forma `-Dnom.propietat=valor`, i PowerShell interpreta malament els punts d'aquests paràmetres: `-Dexec.mainClass=edu.llibreries.Main` arriba a Maven trossejat i falla amb errors estranys.

**Solució: poseu sempre els paràmetres `-D` entre cometes dobles:**

```
mvn exec:java "-Dexec.mainClass=edu.llibreries.Main"
```

Amb cometes funciona igual a PowerShell, a cmd i a Linux/macOS. En aquest document les farem servir sempre.

---

## 2. Creació del projecte

Maven pot generar l'esquelet d'un projecte a partir d'una plantilla anomenada **arquetip** (*archetype*). Farem servir `maven-archetype-quickstart`, l'arquetip d'una aplicació Java senzilla.

Cada projecte Maven s'identifica per tres coordenades, conegudes com a **GAV**:

| Coordenada | Significat | Valor al nostre projecte |
|---|---|---|
| `groupId` | Organització o grup (com un nom de paquet invertit) | `edu.llibreries` |
| `artifactId` | Nom del projecte | `generador-pdf` |
| `version` | Versió | `1.0-SNAPSHOT` |

Feu servir com a `groupId` el mateix prefix de paquet que el nostre codi (`edu.llibreries`): així l'arquetip crearà les classes d'exemple al mateix paquet que les nostres fonts.

Situeu-vos a la carpeta on vulgueu crear el projecte i executeu (tot en una línia):

▶
```
mvn archetype:generate "-DgroupId=edu.llibreries" "-DartifactId=generador-pdf" "-DarchetypeArtifactId=maven-archetype-quickstart" "-DarchetypeVersion=1.5" "-DinteractiveMode=false"
```

- `archetype:generate` és el *goal* que genera projectes (a la [secció 6](#6-plugins-i-goals) veurem què és un *goal*).
- `-DinteractiveMode=false` evita que Maven faci preguntes. Si el traieu, Maven us demanarà les dades una a una.
- La primera vegada, Maven baixarà molts arxius d'Internet. És normal: s'està baixant ell mateix els *plugins* que necessita.

Quan acabi tindreu una carpeta `generador-pdf`. Entreu-hi:

▶
```
cd generador-pdf
```

> **Versió del SNAPSHOT.** El sufix `-SNAPSHOT` indica que és una versió "en desenvolupament", que pot canviar. Una versió sense aquest sufix (per exemple `1.0`) és una versió publicada i, per convenció, no s'ha de modificar mai més.

---

## 3. Estructura de carpetes d'un projecte Maven

L'arquetip ha creat aquesta estructura:

```
generador-pdf/
├── pom.xml                         ← descripció del projecte
├── .mvn/
│   ├── jvm.config                  ← opcions de la JVM de Maven (buit)
│   └── maven.config                ← opcions de Maven per defecte (buit)
└── src/
    ├── main/
    │   └── java/
    │       └── edu/llibreries/
    │           └── App.java        ← codi de l'aplicació
    └── test/
        └── java/
            └── edu/llibreries/
                └── AppTest.java    ← tests
```

Aquesta estructura és una **convenció** de Maven: tots els projectes Maven la segueixen i per això no cal configurar on és el codi.

| Carpeta | Contingut |
|---|---|
| `src/main/java` | Codi font de l'aplicació |
| `src/main/resources` | Recursos que van dins del jar (imatges, `.properties`...) |
| `src/test/java` | Codi dels tests |
| `src/test/resources` | Recursos que només fan servir els tests |
| `target/` | **Tot el que genera Maven** (classes compilades, jars, informes...). Es crea en compilar i es pot esborrar sense por. No s'ha de pujar mai al control de versions. |

Les carpetes `resources` no les crea l'arquetip; les podeu crear quan les necessiteu.

> **Principi de Maven: *convention over configuration*.** Si seguiu les convencions, gairebé no cal configurar res. Si no les seguiu, ho podeu fer, però haureu d'explicar-ho tot al POM.

De moment **no copieu encara les nostres fonts**. Primer treballarem amb `App.java` per entendre el cicle de vida i els *plugins*, i a la [secció 7](#7-afegir-dependències) hi portarem el nostre codi.

---

## 4. L'arxiu POM

El `pom.xml` (*Project Object Model*) és el cor del projecte: descriu **què és** el projecte i **què necessita**. Obriu el que ha generat l'arquetip. Les parts principals són:

```xml
<project ...>
  <modelVersion>4.0.0</modelVersion>

  <!-- 1. Identificació (GAV) -->
  <groupId>edu.llibreries</groupId>
  <artifactId>generador-pdf</artifactId>
  <version>1.0-SNAPSHOT</version>

  <!-- 2. Informació descriptiva (opcional) -->
  <name>generador-pdf</name>
  <url>http://www.example.com</url>

  <!-- 3. Propietats: variables reutilitzables i configuració -->
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.release>17</maven.compiler.release>
  </properties>

  <!-- 4. Gestió de versions (veure més avall) -->
  <dependencyManagement> ... </dependencyManagement>

  <!-- 5. Dependències: llibreries que fa servir el projecte -->
  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
      <scope>test</scope>
    </dependency>
    ...
  </dependencies>

  <!-- 6. Configuració de la construcció: plugins -->
  <build>
    <pluginManagement> ... </pluginManagement>
  </build>
</project>
```

1. **Coordenades GAV**: identifiquen el projecte de manera única al món Maven.
2. **Informació descriptiva**: nom, URL, descripció... No afecta la construcció.
3. **Propietats**:
   - `project.build.sourceEncoding`: codificació dels fitxers font. Important amb els accents del nostre codi!
   - `maven.compiler.release`: versió de Java per a la qual es compila. ⚠️ **Aquí és on heu de posar la vostra versió si no és 17** (vegeu la [secció 1.1](#11-prerequisit-un-jdk)).
   - Una propietat es pot fer servir a qualsevol lloc del POM com a `${nom}`. També n'hi ha de predefinides, com `${project.version}` o `${project.build.directory}` (la carpeta `target`).
4. **`dependencyManagement`**: només **fixa versions** de dependències, però **no les afegeix** al projecte. L'arquetip l'utilitza per importar la "llista de versions" de JUnit (un BOM, *Bill of Materials*): per això les dependències de JUnit de més avall no porten `<version>`.
5. **`dependencies`**: les llibreries que realment fa servir el projecte.
6. **`build`**: configuració dels *plugins* que fan la feina. L'arquetip hi posa un bloc `pluginManagement` que **només fixa les versions** dels plugins.

> ⚠️ **Gestió (`...Management`) vs. ús.** `dependencyManagement` i `pluginManagement` volen dir "*si* es fa servir aquesta dependència/plugin, fes servir aquesta versió i configuració", però no l'activen. Per activar-lo cal declarar-lo a `<dependencies>` o a `<build><plugins>`. És una de les confusions més habituals amb Maven, i ens hi trobarem a la [secció 10](#10-empaquetar-amb-una-carpeta-lib).

Per veure el POM "real" que fa servir Maven, amb tot el que hereta per defecte:

```
mvn help:effective-pom
```

---

## 5. El cicle de vida

Maven organitza la construcció en un **cicle de vida** (*lifecycle*): una seqüència ordenada de **fases**. El cicle principal (*default*) és:

```
 validate ──► compile ──► test ──► package ──► verify ──► install ──► deploy
    │            │          │         │           │          │          │
 comprova     compila    executa   crea el    controls   copia el   puja el
 que el POM   src/main   els tests  .jar a    de qualitat jar al     jar a un
 és correcte             (src/test) target/   addicionals repositori repositori
                                                          local      remot
                                                          (~/.m2)    (empresa)
```

La regla clau: **executar una fase executa totes les anteriors**. `mvn package` fa `validate`, `compile`, `test` i `package`.

Hi ha també un cicle separat, **clean**, que esborra la carpeta `target`.

| Comanda | Què fa |
|---|---|
| `mvn clean` | Esborra `target/` |
| `mvn compile` | Compila `src/main/java` → `target/classes` |
| `mvn test` | Compila i executa els tests |
| `mvn package` | Compila, passa els tests i crea el `.jar` a `target/` |
| `mvn verify` | Com `package`, més controls de qualitat que s'hagin configurat |
| `mvn install` | Com `verify`, i copia el jar al **repositori local** (`~/.m2`) perquè altres projectes del vostre ordinador el puguin fer servir ([secció 12](#12-fer-servir-altres-projectes-locals-propis)) |
| `mvn deploy` | Com `install`, i el puja a un **repositori remot** compartit (Nexus, Artifactory...). No el farem servir: cal tenir un servidor configurat. |
| `mvn clean package` | Es poden combinar: primer neteja i després empaqueta |

Proveu-ho amb el projecte de l'arquetip:

▶
```
mvn clean package
```

A la sortida veureu com s'executa cada pas, i el resultat dels tests:

```
[INFO] --- clean:3.4.0:clean (default-clean) @ generador-pdf ---
[INFO] --- resources:3.3.1:resources (default-resources) @ generador-pdf ---
[INFO] --- compiler:3.13.0:compile (default-compile) @ generador-pdf ---
[INFO] --- resources:3.3.1:testResources (default-testResources) @ generador-pdf ---
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ generador-pdf ---
[INFO] --- surefire:3.3.0:test (default-test) @ generador-pdf ---
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] --- jar:3.4.2:jar (default-jar) @ generador-pdf ---
[INFO] Building jar: ...\generador-pdf\target\generador-pdf-1.0-SNAPSHOT.jar
[INFO] BUILD SUCCESS
```

Si un test falla, Maven s'atura i **no** genera el jar. Per saltar els tests (només quan estigueu segurs del que feu):

```
mvn package "-DskipTests"
```

---

## 6. Plugins i goals

Fixeu-vos en les línies `--- compiler:3.13.0:compile ---` de la sortida anterior. Maven en si no sap compilar, ni executar tests, ni fer jars: **tota la feina la fan *plugins***.

- Un **plugin** és un component (un jar) que aporta funcionalitat. Exemples: `maven-compiler-plugin`, `maven-surefire-plugin` (tests), `maven-jar-plugin`.
- Un **goal** és una tasca concreta d'un plugin. Un plugin pot tenir diversos goals: el `compiler` té `compile` i `testCompile`.

Les **fases** del cicle de vida no fan res per si soles: són "punts" on hi ha **goals enganxats** (*bound*). Per a un projecte que genera un jar, Maven enganxa per defecte:

| Fase | Goal enganxat |
|---|---|
| `compile` | `compiler:compile` |
| `test` | `compiler:testCompile` + `surefire:test` |
| `package` | `jar:jar` |
| `install` | `install:install` |
| `deploy` | `deploy:deploy` |

### Invocar un goal directament

Un goal es pot executar directament amb la sintaxi `prefix:goal`, sense passar per cap fase. Ja n'heu fet servir un: `archetype:generate`.

Un plugin molt útil és l'**Exec Maven Plugin**, que executa una classe `main` amb el classpath que coneix Maven:

▶
```
mvn compile exec:java "-Dexec.mainClass=edu.llibreries.App"
```

```
Hello World!
```

Aquesta comanda combina dues coses:

- `compile` és una **fase**: compila el codi (i les fases anteriors).
- `exec:java` és un **goal**: executa la classe indicada. No compila, per això el posem darrere de `compile`.
- `-Dexec.mainClass=...` és un **paràmetre** del goal, passat com a propietat.

Els paràmetres d'un plugin també es poden fixar al POM, perquè no calgui escriure'ls cada vegada. Ho farem a la [secció 9](#9-executar-el-programa-amb-dependències).

Per saber quins goals i paràmetres té un plugin:

```
mvn help:describe "-Dplugin=exec"
```

---

## 7. Afegir dependències

### 7.1 Portar el nostre codi al projecte

Ara sí, substituïm l'exemple per les fonts reals:

1. **Esborreu** `src\main\java\edu\llibreries\App.java` i `src\test\java\edu\llibreries\AppTest.java`.
2. **Copieu** el contingut de la carpeta `src` de l'enunciat (la carpeta `edu` amb tot el que conté) dins de `src\main\java`.

Ha de quedar així:

```
src/main/java/edu/llibreries/
├── Main.java
├── pdf/GeneradorPdf.java
└── text/FormatadorText.java
```

Proveu de compilar:

▶
```
mvn compile
```

```
[ERROR] COMPILATION ERROR :
[ERROR] .../GeneradorPdf.java:[3,33] package org.apache.pdfbox.pdmodel does not exist
...
[ERROR] .../FormatadorText.java:[3,31] package org.apache.commons.text does not exist
```

És el mateix error que vau tenir quan compilàveu a mà sense posar els jars al classpath. La diferència és com ho resolem ara.

### 7.2 Buscar dependències a Maven Repository

Les llibreries es publiquen a **Maven Central**, el repositori públic d'on Maven les baixa automàticament. Per cercar-hi farem servir **<https://mvnrepository.com/>**:

1. Cerqueu **pdfbox**.
2. Entreu a **Apache PDFBox** (`org.apache.pdfbox » pdfbox`). Compte, hi ha molts artefactes amb noms semblants (`pdfbox-app`, `pdfbox-tools`...); el que volem és exactament `pdfbox`.
3. Trieu la versió **3.0.8** (o la 3.0.x més recent).
   ⚠️ Ha de ser una **3.x**: el nostre codi fa servir `Standard14Fonts`, que no existeix a la 2.x. La versió importa!
4. A la pestanya **Maven**, copieu el fragment XML:

```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.8</version>
</dependency>
```

Feu el mateix per a **Apache Commons Text** (`org.apache.commons » commons-text`), versió **1.15.0**.

> Alternativa oficial per cercar: <https://central.sonatype.com/>. Mvnrepository és un lloc extern però molt còmode, i a més mostra avisos de vulnerabilitats conegudes de cada versió.

### 7.3 Afegir-les al POM

Afegiu els dos fragments dins de `<dependencies>` (el que **no** és dins de `dependencyManagement`):

```xml
  <dependencies>
    <dependency>
      <groupId>org.apache.pdfbox</groupId>
      <artifactId>pdfbox</artifactId>
      <version>3.0.8</version>
    </dependency>
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-text</artifactId>
      <version>1.15.0</version>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
      <scope>test</scope>
    </dependency>
    ...
  </dependencies>
```

▶
```
mvn compile
```

Maven baixa les llibreries i ara compila sense errors.

### 7.4 On són els jars? El repositori local

Maven guarda tot el que baixa al **repositori local**, a la carpeta de l'usuari:

```
C:\Users\<usuari>\.m2\repository\
└── org\apache\pdfbox\pdfbox\3.0.8\
    ├── pdfbox-3.0.8.jar
    └── pdfbox-3.0.8.pom
```

És el mateix `.jar` que vau baixar a mà. La diferència és que ara Maven el baixa **una sola vegada** i el reaprofita per a tots els projectes de l'ordinador.

### 7.5 L'abast (*scope*) de les dependències

Fixeu-vos que les dependències de JUnit tenen `<scope>test</scope>`. L'abast indica **quan** es necessita una dependència:

| Scope | Compilar | Tests | Executar | Exemple |
|---|---|---|---|---|
| `compile` (per defecte) | ✔ | ✔ | ✔ | pdfbox, commons-text |
| `test` | | ✔ | | JUnit |
| `runtime` | | ✔ | ✔ | un driver JDBC |
| `provided` | ✔ | ✔ | (l'aporta l'entorn) | API de servlets en un servidor web |

### 7.6 Afegim un test

El projecte no tenia tests. Afegim-ne un per a `FormatadorText`, que és fàcil de provar. Creeu `src\test\java\edu\llibreries\text\FormatadorTextTest.java`:

```java
package edu.llibreries.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class FormatadorTextTest {

    @Test
    void titolPosaMajusculaACadaParaula() {
        FormatadorText f = new FormatadorText(70);
        assertEquals("Hola Món Des De Java", f.titol("hola MÓN des de java"));
    }

    @Test
    void capLiniaSuperaLAmpleIndicat() {
        FormatadorText f = new FormatadorText(10);
        List<String> linies = f.parteixEnLinies("una frase prou llarga per haver de partir-la");
        for (String linia : linies) {
            assertTrue(linia.length() <= 10, "Línia massa llarga: " + linia);
        }
    }
}
```

Fixeu-vos que el test és al **mateix paquet** que la classe que prova, però a `src/test/java`.

▶
```
mvn clean package
```

```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Proveu de trencar un test (per exemple, canvieu `"Hola Món Des De Java"` per `"Hola món"`) i torneu a executar `mvn package`: Maven s'aturarà i no generarà el jar. Després deixeu-lo bé.

---

## 8. L'arbre de dependències

Nosaltres hem declarat **2** dependències. Però quantes jars vau haver de baixar a mà? Vegem-ho:

▶
```
mvn dependency:tree
```

```
[INFO] edu.llibreries:generador-pdf:jar:1.0-SNAPSHOT
[INFO] +- org.apache.pdfbox:pdfbox:jar:3.0.8:compile
[INFO] |  +- org.apache.pdfbox:pdfbox-io:jar:3.0.8:compile
[INFO] |  +- org.apache.pdfbox:fontbox:jar:3.0.8:compile
[INFO] |  \- commons-logging:commons-logging:jar:1.4.0:compile
[INFO] +- org.apache.commons:commons-text:jar:1.15.0:compile
[INFO] |  \- org.apache.commons:commons-lang3:jar:3.20.0:compile
[INFO] +- org.junit.jupiter:junit-jupiter-api:jar:5.11.0:test
[INFO] |  +- org.opentest4j:opentest4j:jar:1.3.0:test
[INFO] |  +- org.junit.platform:junit-platform-commons:jar:1.11.0:test
[INFO] |  \- org.apiguardian:apiguardian-api:jar:1.1.2:test
[INFO] \- org.junit.jupiter:junit-jupiter-params:jar:5.11.0:test
```

- Les dependències del primer nivell (`+-` a l'esquerra de tot) són les que **hem declarat**: són **directes**.
- Les de nivells inferiors són **transitives**: les necessiten les nostres dependències, i Maven les ha trobat llegint el POM de cada llibreria.

Per executar el programa calen **6 jars** (pdfbox, pdfbox-io, fontbox, commons-logging, commons-text i commons-lang3). Nosaltres n'hem declarat 2, i Maven ha resolt la resta. Aquest és el gran avantatge respecte a la compilació manual.

> Si dues llibreries necessiten versions diferents d'una mateixa dependència, Maven en tria una (la més propera a l'arrel de l'arbre). `dependency:tree` és l'eina per detectar aquests conflictes.

---

## 9. Executar el programa amb dependències

### 9.1 Directament amb `java`: no funciona

Amb `mvn package` ja tenim `target\generador-pdf-1.0-SNAPSHOT.jar`. Provem d'executar-lo:

▶
```
java -jar target\generador-pdf-1.0-SNAPSHOT.jar
```

```
no main manifest attribute, in target\generador-pdf-1.0-SNAPSHOT.jar
```

El jar no indica quina és la classe principal. Indiquem-la a mà:

▶
```
java -cp target\generador-pdf-1.0-SNAPSHOT.jar edu.llibreries.Main
```

```
Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/commons/text/WordUtils
	at edu.llibreries.text.FormatadorText.titol(FormatadorText.java:22)
	at edu.llibreries.Main.main(Main.java:34)
```

Ara troba `Main`, però no les llibreries. **És normal**: per defecte, Maven genera un jar que només conté **les nostres classes**. És un jar pensat per ser una llibreria, que altres projectes Maven faran servir: aquests projectes llegiran el nostre POM per saber quines dependències calen.

Per executar-lo tenim tres opcions: fer-ho a través de Maven (9.2), empaquetar amb una carpeta `lib` ([secció 10](#10-empaquetar-amb-una-carpeta-lib)) o fer un *fat jar* ([secció 11](#11-empaquetar-en-un-fat-jar)).

### 9.2 A través de Maven amb `exec:java`

Maven sí que coneix el classpath complet:

▶
```
mvn compile exec:java "-Dexec.mainClass=edu.llibreries.Main"
```

```
PDF creat correctament: sortida.pdf
```

El PDF es crea a la carpeta des d'on executeu la comanda.

> La primera vegada, PDFBox pot mostrar missatges `INFO` sobre la construcció d'una memòria cau de fonts del sistema. És normal i només passa una vegada.

Per passar arguments al programa:

▶
```
mvn exec:java "-Dexec.mainClass=edu.llibreries.Main" "-Dexec.args=prova.pdf Hola des de Maven"
```

### 9.3 Fixar la classe principal al POM

Per no haver d'escriure la classe principal cada vegada, afegiu-la com a propietat:

```xml
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.release>17</maven.compiler.release>
    <exec.mainClass>edu.llibreries.Main</exec.mainClass>
  </properties>
```

És una bona pràctica fixar també la versió del plugin, perquè la construcció no canviï si surt una versió nova. Afegiu, dins de `<build>` i **després** de `</pluginManagement>`, un bloc `<plugins>`:

```xml
  <build>
    <pluginManagement>
      ... (el que ja hi havia)
    </pluginManagement>

    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>3.6.4</version>
      </plugin>
    </plugins>
  </build>
```

▶
```
mvn compile exec:java
```

Aquesta propietat `exec.mainClass` també la fa servir NetBeans ([secció 14](#14-treballar-amb-netbeans)).

`exec:java` és molt pràctic mentre desenvolupeu, però **no serveix per distribuir** el programa: qui el vulgui executar hauria de tenir Maven i el codi font. Per això cal empaquetar-lo bé.

---

## 10. Empaquetar amb una carpeta `lib`

Volem un jar executable amb `java -jar` que trobi les llibreries en una carpeta `lib` al seu costat:

```
target/
├── generador-pdf-1.0-SNAPSHOT.jar
└── lib/
    ├── pdfbox-3.0.8.jar
    ├── ...
```

Cal fer dues coses:

1. Que el `MANIFEST.MF` del jar inclogui `Main-Class` (la classe principal) i `Class-Path` (la llista de jars de `lib`). Ho fa el **maven-jar-plugin**.
2. Que es copiïn les dependències a `target\lib`. Ho fa el **maven-dependency-plugin**.

Afegiu aquests dos plugins al bloc `<plugins>` (al costat del d'exec):

```xml
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>3.6.4</version>
      </plugin>

      <!-- Afegeix Main-Class i Class-Path al MANIFEST.MF -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <configuration>
          <archive>
            <manifest>
              <mainClass>edu.llibreries.Main</mainClass>
              <addClasspath>true</addClasspath>
              <classpathPrefix>lib/</classpathPrefix>
            </manifest>
          </archive>
        </configuration>
      </plugin>

      <!-- Copia les dependències a target/lib durant la fase package -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.11.0</version>
        <executions>
          <execution>
            <id>copia-dependencies</id>
            <phase>package</phase>
            <goals>
              <goal>copy-dependencies</goal>
            </goals>
            <configuration>
              <outputDirectory>${project.build.directory}/lib</outputDirectory>
              <includeScope>runtime</includeScope>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
```

Detalls importants:

- **El `maven-jar-plugin` no porta `<version>`**: la hereta del `pluginManagement` de l'arquetip. Aquí és on es veu per a què serveix el `pluginManagement`.
- **`<executions>`** enganxa el goal `copy-dependencies` a la fase `package`. El `maven-jar-plugin` no ho necessita, perquè ja està enganxat a `package` per defecte (taula de la [secció 6](#6-plugins-i-goals)).
- **`includeScope runtime`** copia les dependències necessàries per executar el programa, però no JUnit (scope `test`).
- **`${project.build.directory}`** és la carpeta `target`.

> ⚠️ **Compte amb `pluginManagement`.** Els plugins s'han de posar a `<build><plugins>`, **no** dins de `<pluginManagement><plugins>`. Si poseu el `maven-dependency-plugin` dins de `pluginManagement`, Maven no l'executarà i no apareixerà cap carpeta `lib`, sense cap error. En canvi, el `maven-jar-plugin` funcionaria igualment, perquè s'executa sempre. És un error molt habitual i difícil de veure.

▶
```
mvn clean package
```

A la sortida ha d'aparèixer aquesta línia:

```
[INFO] --- dependency:3.11.0:copy-dependencies (copia-dependencies) @ generador-pdf ---
```

Comproveu el resultat:

▶
```
dir target\lib
```

```
commons-lang3-3.20.0.jar
commons-logging-1.4.0.jar
commons-text-1.15.0.jar
fontbox-3.0.8.jar
pdfbox-3.0.8.jar
pdfbox-io-3.0.8.jar
```

Són exactament els 6 jars de l'arbre de dependències, i sense els de test. El `MANIFEST.MF` del jar (el podeu obrir amb qualsevol programa de zip, a `META-INF\MANIFEST.MF`) ara conté:

```
Manifest-Version: 1.0
Created-By: Maven JAR Plugin 3.4.2
Build-Jdk-Spec: 17
Class-Path: lib/pdfbox-3.0.8.jar lib/pdfbox-io-3.0.8.jar lib/fontbox-3.0
 .8.jar lib/commons-logging-1.4.0.jar lib/commons-text-1.15.0.jar lib/co
 mmons-lang3-3.20.0.jar
Main-Class: edu.llibreries.Main
```

I ara sí:

▶
```
java -jar target\generador-pdf-1.0-SNAPSHOT.jar
```

```
PDF creat correctament: sortida.pdf
```

Els camins del `Class-Path` són **relatius a la ubicació del jar**, no a la carpeta des d'on l'executeu. Per distribuir el programa cal copiar **el jar i la carpeta `lib`** junts.

---

## 11. Empaquetar en un *fat jar*

Una alternativa és un ***fat jar*** (o *uber jar*): **un sol jar** que conté les nostres classes **i** les de totes les dependències, desempaquetades i tornades a empaquetar juntes. Ho fa el **maven-shade-plugin**. Afegiu-lo al bloc `<plugins>`:

```xml
      <!-- Genera un "fat jar": target/generador-pdf-1.0-SNAPSHOT-all.jar -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.6.2</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
            <configuration>
              <shadedArtifactAttached>true</shadedArtifactAttached>
              <shadedClassifierName>all</shadedClassifierName>
              <createDependencyReducedPom>false</createDependencyReducedPom>
              <transformers>
                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                  <mainClass>edu.llibreries.Main</mainClass>
                </transformer>
                <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
              </transformers>
            </configuration>
          </execution>
        </executions>
      </plugin>
```

- **`shadedArtifactAttached` + `shadedClassifierName`**: genera el fat jar com un arxiu **a part**, amb el sufix `-all`. Sense això, el shade substituiria el jar normal. Així els podem comparar.
- **`ManifestResourceTransformer`**: escriu el `Main-Class` al manifest.
- **`ServicesResourceTransformer`**: fusiona els fitxers `META-INF/services`. Algunes llibreries (drivers JDBC, sistemes de log...) en depenen i, sense fusionar-los, deixen de funcionar sense avisar. Al nostre programa no és imprescindible, però és bona pràctica posar-lo sempre.
- **`createDependencyReducedPom false`**: evita que el plugin creï un `dependency-reduced-pom.xml` a l'arrel del projecte, que aquí no necessitem.

▶
```
mvn clean package
```

Veureu avisos com aquests:

```
[WARNING] fontbox-3.0.8.jar, pdfbox-3.0.8.jar, pdfbox-io-3.0.8.jar define 3 overlapping resources:
[WARNING]   - META-INF/DEPENDENCIES
[WARNING]   - META-INF/LICENSE
[WARNING]   - META-INF/NOTICE
...
```

En ajuntar-ho tot en un sol jar, hi ha arxius amb el mateix nom a diferents llibreries (llicències, manifests...), i només se'n pot quedar un. Aquí no afecten el funcionament, però **és el primer lloc on mirar si una llibreria funciona amb `lib/` i falla dins d'un fat jar**.

Compareu les mides:

▶
```
dir target\*.jar
```

```
  4.808.737 generador-pdf-1.0-SNAPSHOT-all.jar
      7.136 generador-pdf-1.0-SNAPSHOT.jar
```

I executeu el fat jar. Podeu copiar-lo **sol** a qualsevol altra carpeta:

▶
```
java -jar target\generador-pdf-1.0-SNAPSHOT-all.jar
```

```
PDF creat correctament: sortida.pdf
```

### Carpeta `lib` o fat jar?

| | Jar + `lib/` | Fat jar |
|---|---|---|
| Distribució | Una carpeta | Un sol fitxer |
| Actualitzar una llibreria | Substituir-ne un jar | Recompilar-ho tot |
| Conflictes d'arxius repetits | No n'hi ha | Possibles (avisos del shade) |
| Llibreries signades digitalment | Funcionen | Cal excloure les signatures |
| Ús típic | Aplicacions instal·lades | Eines de línia de comandes, microserveis |

---

## 12. Fer servir altres projectes locals propis

Fins ara totes les dependències venien de Maven Central. Però sovint voldreu reutilitzar **codi propi** d'un altre projecte. Per veure-ho, **dividirem el projecte en dos**:

- **`formatador-text`**: una llibreria nostra amb la classe `FormatadorText` (que depèn de Commons Text).
- **`generador-pdf`**: l'aplicació, que farà servir la llibreria `formatador-text`.

Hi ha dues maneres de connectar-los: **instal·lar** la llibreria al repositori local (12.1), o crear un **projecte multimòdul** (12.2).

### 12.1 Instal·lar un projecte i referenciar-lo

#### Crear la llibreria

Des de la carpeta que **conté** `generador-pdf` (no des de dins), creeu el nou projecte. Aquesta vegada indiquem el paquet amb `-Dpackage`:

▶
```
cd ..
mvn archetype:generate "-DgroupId=edu.llibreries" "-DartifactId=formatador-text" "-Dpackage=edu.llibreries.text" "-DarchetypeArtifactId=maven-archetype-quickstart" "-DarchetypeVersion=1.5" "-DinteractiveMode=false"
```

Ara teniu dues carpetes, una al costat de l'altra:

```
generador-pdf/
formatador-text/
```

A `formatador-text`:

1. Esborreu `App.java` i `AppTest.java`.
2. **Moveu** `FormatadorText.java` des de `generador-pdf\src\main\java\edu\llibreries\text\` a `formatador-text\src\main\java\edu\llibreries\text\`.
3. **Moveu** `FormatadorTextTest.java` des de `generador-pdf\src\test\java\edu\llibreries\text\` a `formatador-text\src\test\java\edu\llibreries\text\`.
4. Al `pom.xml` de `formatador-text`, afegiu la dependència de Commons Text a `<dependencies>`:

```xml
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-text</artifactId>
      <version>1.15.0</version>
    </dependency>
```

Fixeu-vos que la dependència de Commons Text **se'n va amb el codi que la fa servir**.

#### Instal·lar-la

▶
```
cd formatador-text
mvn install
```

```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] --- install:3.1.2:install (default-install) @ formatador-text ---
[INFO] Installing ...\formatador-text\pom.xml to C:\Users\<usuari>\.m2\repository\edu\llibreries\formatador-text\1.0-SNAPSHOT\formatador-text-1.0-SNAPSHOT.pom
[INFO] Installing ...\formatador-text\target\formatador-text-1.0-SNAPSHOT.jar to C:\Users\<usuari>\.m2\repository\edu\llibreries\formatador-text\1.0-SNAPSHOT\formatador-text-1.0-SNAPSHOT.jar
[INFO] BUILD SUCCESS
```

`install` ha copiat el jar **i el POM** de la llibreria al repositori local, al mateix lloc on hi ha pdfbox. Per a Maven, a partir d'ara la nostra llibreria és una dependència com qualsevol altra.

#### Fer-la servir des de l'aplicació

Al `pom.xml` de `generador-pdf`, **substituïu** la dependència de `commons-text` per la de la nostra llibreria:

```xml
    <dependency>
      <groupId>edu.llibreries</groupId>
      <artifactId>formatador-text</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
```

▶
```
cd ..\generador-pdf
mvn dependency:tree
```

```
[INFO] edu.llibreries:generador-pdf:jar:1.0-SNAPSHOT
[INFO] +- org.apache.pdfbox:pdfbox:jar:3.0.8:compile
[INFO] |  +- org.apache.pdfbox:pdfbox-io:jar:3.0.8:compile
[INFO] |  +- org.apache.pdfbox:fontbox:jar:3.0.8:compile
[INFO] |  \- commons-logging:commons-logging:jar:1.4.0:compile
[INFO] +- edu.llibreries:formatador-text:jar:1.0-SNAPSHOT:compile
[INFO] |  \- org.apache.commons:commons-text:jar:1.15.0:compile
[INFO] |     \- org.apache.commons:commons-lang3:jar:3.20.0:compile
...
```

Ara `commons-text` arriba **de manera transitiva a través de la nostra llibreria**, perquè Maven ha llegit el POM que vam instal·lar.

▶
```
mvn clean package
java -jar target\generador-pdf-1.0-SNAPSHOT-all.jar
```

A `target\lib` ara hi ha també `formatador-text-1.0-SNAPSHOT.jar`.

> ⚠️ **El problema de l'`install`.** Si modifiqueu `formatador-text` i **no** torneu a fer `mvn install`, `generador-pdf` continuarà fent servir **la versió antiga** que hi ha a `.m2`, sense cap avís. És una font clàssica de "però si ja ho he arreglat!". Proveu-ho: canvieu el mètode `titol` perquè retorni el text en majúscules, feu `mvn package` a `generador-pdf` i veureu que el PDF no canvia fins que feu `mvn install` a `formatador-text`.

Aquesta manera de treballar és adequada quan la llibreria té **vida pròpia** i la fan servir diversos projectes. Quan els dos projectes evolucionen **junts**, és millor el següent mecanisme.

### 12.2 Projecte multimòdul

Un **projecte multimòdul** agrupa diversos projectes (**mòduls**) sota un **POM pare**. Maven els construeix tots junts, en l'ordre correcte, i sense haver de fer `install`.

#### Estructura

Creeu una carpeta `exercici-maven` i **moveu-hi** les dues carpetes de projecte:

```
exercici-maven/
├── pom.xml                ← POM pare (nou)
├── formatador-text/
│   ├── pom.xml
│   └── src/...
└── generador-pdf/
    ├── pom.xml
    └── src/...
```

#### El POM pare

Creeu `exercici-maven\pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>edu.llibreries</groupId>
  <artifactId>exercici-maven</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>pom</packaging>

  <!-- AGREGACIÓ: quins projectes es construeixen junts -->
  <modules>
    <module>formatador-text</module>
    <module>generador-pdf</module>
  </modules>

  <!-- HERÈNCIA: tot el que hi ha a partir d'aquí ho hereten els fills -->
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.release>17</maven.compiler.release>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.junit</groupId>
        <artifactId>junit-bom</artifactId>
        <version>5.11.0</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
      <!-- Versions centralitzades: els fills no cal que les repeteixin -->
      <dependency>
        <groupId>edu.llibreries</groupId>
        <artifactId>formatador-text</artifactId>
        <version>${project.version}</version>
      </dependency>
      <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-text</artifactId>
        <version>1.15.0</version>
      </dependency>
      <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>3.0.8</version>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <!-- Dependències que tenen TOTS els mòduls -->
  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <artifactId>maven-clean-plugin</artifactId>
          <version>3.4.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-resources-plugin</artifactId>
          <version>3.3.1</version>
        </plugin>
        <plugin>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.13.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>3.3.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-jar-plugin</artifactId>
          <version>3.4.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-install-plugin</artifactId>
          <version>3.1.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-deploy-plugin</artifactId>
          <version>3.1.2</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```

- **`<packaging>pom</packaging>`**: aquest projecte no genera cap jar; només serveix per organitzar.
- Hi hem traslladat tot el que **es repetia** als dos POM fills: les propietats, el BOM de JUnit i el `pluginManagement`.
- Al `dependencyManagement` hi hem posat les **versions** de les llibreries. Així totes les versions del projecte són en un sol lloc, i els fills no les repeteixen.

Aquest POM fa dues coses **independents**, que normalment van juntes:

| Mecanisme | On es declara | Què vol dir |
|---|---|---|
| **Agregació** | `<modules>` al pare | "Quan em construeixis a mi, construeix també aquests projectes" |
| **Herència** | `<parent>` als fills | "Agafa la configuració del pare" |

#### Els POM fills

Gràcies a l'herència, els POM dels fills queden molt més curts. Substituïu `formatador-text\pom.xml` per:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>edu.llibreries</groupId>
    <artifactId>exercici-maven</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>

  <!-- groupId i version s'hereten del pare -->
  <artifactId>formatador-text</artifactId>

  <dependencies>
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-text</artifactId>
      <!-- sense version: la diu el dependencyManagement del pare -->
    </dependency>
  </dependencies>
</project>
```

I `generador-pdf\pom.xml` per:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>edu.llibreries</groupId>
    <artifactId>exercici-maven</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>

  <artifactId>generador-pdf</artifactId>

  <properties>
    <exec.mainClass>edu.llibreries.Main</exec.mainClass>
  </properties>

  <dependencies>
    <dependency>
      <groupId>edu.llibreries</groupId>
      <artifactId>formatador-text</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.pdfbox</groupId>
      <artifactId>pdfbox</artifactId>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <!-- Aquí, sense canvis, el bloc <plugins> de les seccions 9, 10 i 11:
           exec-maven-plugin, maven-jar-plugin, maven-dependency-plugin
           i maven-shade-plugin -->
    </plugins>
  </build>
</project>
```

Fixeu-vos que el bloc `<plugins>` es queda al fill `generador-pdf`, perquè només l'aplicació s'ha d'empaquetar amb les dependències. La llibreria es genera com un jar normal.

#### Construir-ho tot

Des de la carpeta `exercici-maven`:

▶
```
mvn clean package
```

```
[INFO] Reactor Build Order:
[INFO]
[INFO] exercici-maven                                                     [pom]
[INFO] formatador-text                                                    [jar]
[INFO] generador-pdf                                                      [jar]
...
[INFO] Reactor Summary for exercici-maven 1.0-SNAPSHOT:
[INFO]
[INFO] exercici-maven ..................................... SUCCESS
[INFO] formatador-text .................................... SUCCESS
[INFO] generador-pdf ...................................... SUCCESS
[INFO] BUILD SUCCESS
```

El component de Maven que gestiona els mòduls s'anomena ***reactor***. Ha analitzat les dependències entre mòduls i ha decidit l'ordre: primer `formatador-text`, perquè `generador-pdf` en depèn. A més, `generador-pdf` fa servir **directament** el jar acabat de compilar de `formatador-text`: no cal `install` i no hi ha el problema de la versió antiga.

▶
```
java -jar generador-pdf\target\generador-pdf-1.0-SNAPSHOT-all.jar
```

Comandes útils en un multimòdul:

| Comanda | Què fa |
|---|---|
| `mvn package` | Construeix tots els mòduls |
| `mvn -pl generador-pdf -am package` | Només `generador-pdf` i els mòduls dels quals depèn (`-am`, *also make*) |
| `mvn -pl formatador-text -amd package` | `formatador-text` i els mòduls que en depenen (`-amd`, *also make dependents*) |

> ⚠️ **`exec:java` en un multimòdul.** Si executeu `mvn compile exec:java` des de l'arrel, el goal s'intenta executar a **tots** els mòduls, inclòs el pare, i falla perquè aquest no té classe principal. Les opcions són executar el jar amb `java -jar` (com a dalt), o bé fer `mvn install` a l'arrel i després `mvn exec:java` des de dins de la carpeta `generador-pdf`.

### 12.3 Quan fer servir cada opció

| | `install` + dependència | Multimòdul |
|---|---|---|
| Relació entre projectes | Independents, cicles de vida separats | Evolucionen junts |
| Canvis a la llibreria | Cal `mvn install` cada vegada | Es recullen automàticament |
| Versions | Cadascun la seva | Normalment comparteixen versió |
| Configuració compartida | No | Sí (al POM pare) |
| Exemple | Una llibreria d'utilitats que fan servir diversos projectes | Les capes d'una mateixa aplicació |

---

## 13. El Maven Wrapper

Cada membre d'un equip pot tenir instal·lada una versió diferent de Maven, i això pot fer que el projecte es construeixi diferent a cada ordinador. El **Maven Wrapper** resol aquest problema: afegeix al projecte uns scripts que **baixen i fan servir automàticament una versió concreta de Maven**. Qui baixi el projecte ni tan sols necessita tenir Maven instal·lat; n'hi ha prou amb un JDK.

Des de l'arrel del projecte (`exercici-maven`):

▶
```
mvn -N wrapper:wrapper
```

(`-N`, *non-recursive*, fa que el goal s'executi només al projecte pare i no a cada mòdul.)

Es creen aquests arxius:

```
exercici-maven/
├── mvnw                                ← script per a Linux/macOS
├── mvnw.cmd                            ← script per a Windows
└── .mvn/wrapper/
    └── maven-wrapper.properties        ← quina versió de Maven cal fer servir
```

`maven-wrapper.properties` conté la URL de la versió de Maven que s'ha fixat (la que estàveu fent servir):

```
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.10.0/apache-maven-3.10.0-bin.zip
```

A partir d'ara, en lloc de `mvn` feu servir l'script:

▶
```
mvnw.cmd clean package          (a cmd)
.\mvnw clean package            (a PowerShell)
```

La primera vegada baixa Maven a `C:\Users\<usuari>\.m2\wrapper\`; les següents el reaprofita. Aquests arxius **s'han de pujar al control de versions** junt amb el codi, perquè tothom faci servir la mateixa versió de Maven.

---

## 14. Treballar amb NetBeans

NetBeans entén els projectes Maven de manera nativa: **el `pom.xml` és el projecte**, no cal importar res ni crear cap projecte de NetBeans.

### Obrir el projecte

**File → Open Project...** i seleccioneu la carpeta que conté el `pom.xml` (el NetBeans hi mostra una icona especial de projecte Maven).

- Si obriu el **projecte pare** d'un multimòdul (`exercici-maven`), NetBeans mostra els mòduls a **Modules**. Feu-hi clic dret → **Open Required Projects** per obrir-los tots.
- Als nodes **Dependencies** i **Test Dependencies** del projecte veureu les dependències, incloses les transitives, igual que amb `dependency:tree`.
- Els canvis al `pom.xml` es carreguen automàticament en desar-lo.

### Construir i executar

Les accions del menú de NetBeans són crides a Maven, i la sortida que veieu a la finestra **Output** és la de Maven:

| Acció de NetBeans | Comanda Maven equivalent |
|---|---|
| **Build** | `mvn install` |
| **Clean and Build** | `mvn clean install` |
| **Run** | compila i executa la classe principal amb el plugin Exec (fa servir la propietat `exec.mainClass`) |
| **Test** | `mvn test` |

Fixeu-vos que **Build** fa `install`, no `package`: per això, en un multimòdul, NetBeans no té el problema de la versió antiga de la [secció 12.1](#121-installar-un-projecte-i-referenciar-lo).

Si a l'executar el projecte NetBeans us demana quina és la classe principal, és que no troba la propietat `exec.mainClass` (vegeu la [secció 9.3](#93-fixar-la-classe-principal-al-pom)). Trieu `edu.llibreries.Main` i NetBeans la desarà al projecte.

Per executar goals que no són al menú (per exemple `dependency:tree`): clic dret al projecte → **Run Maven → Goals...**

### Quin Maven fa servir NetBeans?

NetBeans porta **la seva pròpia còpia de Maven**. Perquè faci servir la que heu instal·lat (i així el terminal i l'IDE es comportin igual), aneu a **Tools → Options → Java → Maven** i, a **Maven Home**, trieu la carpeta d'instal·lació (`C:\eines\apache-maven-3.10.0`). Si el projecte té Maven Wrapper, NetBeans el pot fer servir directament.

### Crear un projecte Maven des de NetBeans

**File → New Project → Java with Maven → Java Application** crea un projecte equivalent al que hem fet amb `archetype:generate`. És útil, però és bo saber fer-ho des de la línia de comandes: **la línia de comandes és la referència**. Si un projecte compila a NetBeans però no amb `mvn package`, el que està malament és el projecte.

---

## 15. Resum de comandes

| Comanda | Per a què |
|---|---|
| `mvn -v` | Comprovar la instal·lació |
| `mvn archetype:generate "-DgroupId=..." "-DartifactId=..." "-DarchetypeArtifactId=maven-archetype-quickstart" "-DarchetypeVersion=1.5" "-DinteractiveMode=false"` | Crear un projecte |
| `mvn clean` | Esborrar `target/` |
| `mvn compile` | Compilar |
| `mvn test` | Compilar i passar els tests |
| `mvn package` | Generar el jar (i, si s'ha configurat, `lib/` i el fat jar) |
| `mvn package "-DskipTests"` | Generar el jar sense passar els tests |
| `mvn install` | Copiar el jar al repositori local `~/.m2` |
| `mvn compile exec:java "-Dexec.mainClass=..."` | Executar la classe principal amb les dependències |
| `mvn exec:java "-Dexec.args=..."` | Executar passant arguments al programa |
| `mvn dependency:tree` | Veure l'arbre de dependències |
| `mvn help:effective-pom` | Veure el POM complet, amb tot el que s'hereta |
| `mvn -pl <mòdul> -am package` | Construir un mòdul i els que necessita |
| `mvn -N wrapper:wrapper` | Afegir el Maven Wrapper al projecte |

### Errors habituals

| Símptoma | Causa probable |
|---|---|
| `'mvn' is not recognized...` | Maven no és al `PATH`, o no heu obert un terminal nou |
| Errors estranys amb paràmetres `-D...` a PowerShell | Falten les cometes: `"-Dnom=valor"` |
| `package ... does not exist` en compilar | Falta la dependència al POM |
| Una dependència declarada no s'aplica | És dins de `dependencyManagement` en lloc de `dependencies` |
| Un plugin no s'executa i no dona cap error | És dins de `pluginManagement` en lloc de `plugins` |
| `no main manifest attribute` | El jar no té `Main-Class`: [secció 10](#10-empaquetar-amb-una-carpeta-lib) |
| `NoClassDefFoundError` amb `java -jar` | Les dependències no són al classpath: [seccions 10](#10-empaquetar-amb-una-carpeta-lib) o [11](#11-empaquetar-en-un-fat-jar) |
| Els canvis a una llibreria pròpia no es veuen | No heu tornat a fer `mvn install` a la llibreria |
| `release version 17 not supported` | El JDK és més antic que el 17: canvieu `maven.compiler.release` o actualitzeu el JDK |
