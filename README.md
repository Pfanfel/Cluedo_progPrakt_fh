
# Programmierpraktikum  Cluedo

Auf Basis der folgenden Aufgabenstellung wurde dieses Projekt im laufe eines Semesters erstellt.

Die Anleitung sowie die Dokumentation zu diesem Projekt wurde [hier](Dokumantation%20Programmierpraktikum%20Cluedo%20WS20%20Michael%20Smirnov.pdf) hinterlegt.
Die ausfübrbare `.jar` befindet sich [hier](./final-binaries/Cluedo_Smirnov_ws20.jar).

## Learnings

- Entwicklung eines größeren objektorientierten Projekts
- Erstellung einer GUI mit Java und SceneBuilder
- Animationen mithilfe von selbst implmentierten threadbasierten Koroutinen (mehr dazu in der [Dokumentation](Dokumantation%20Programmierpraktikum%20Cluedo%20WS20%20Michael%20Smirnov.pdf) im Kapitel 3.2.2.3)
- Verschieden Wegfindungsalgorithmen wie die Breitensuche, Dijkstra und A*
- Speichern und Laden von `.json` Spielständen 
- Erstellung drei verschiedener KIs (dumm, mittel, schlau)

## Aufgabenstellung

Zu implementieren ist eine Spielvariante des bekannten Brettspiels
"[Cluedo](https://de.wikipedia.org/wiki/Cluedo)": Im Landhaus von Graf
Eutin ist ein Mord geschehen! Aber wer ist der Täter? Und mit welcher
Tatwaffe wurde die Tat in welchem Raum des Hauses verübt? Detektivische
Fähigkeiten sind zur Lösung gefragt!

Zum besseren Verständnis gibt es hier eine
[Beispiellösung](https://intern.fh-wedel.de/fileadmin/mitarbeiter/ne/SA/WS2021/Cluedo.exe)
(die KI ist noch in Arbeit), die in Delphi umgesetzt wurde. Bindend
bleibt aber in jedem Fall diese Aufgabenstellung - auch und gerade wenn
das Beispielprogramm einige Dinge abweichend umsetzt!

Wer möchte darf auch diesen
[Spielplan](https://intern.fh-wedel.de/fileadmin/mitarbeiter/ne/SA/WS2021/Spielplan.jpg)
verwenden. Ein anderer Plan kann nur verwendet werden, wenn alle
Positionen und Benennungen gleich sind.

### Spielregeln

Es sollen die
[Originalspielregeln](https://intern.fh-wedel.de/fileadmin/mitarbeiter/ne/SA/WS2021/Spielanleitung.pdf)
umgesetzt werden für ein Spiel mit 3 bis 6 Spielern, wobei ein
menschlicher Spieler gegen Computergegner spielt. Es gelten folgende
Abweichungen von den Spielregeln bzw.
Klarstellungen:

-   der menschliche Spieler spielt immer Fräulein Ming (rot), die
    Computergegner (je nach ihrer Anzahl) spielen mit Oberst von Gatow
    (gelb), Frau Weiß (weiß), Direktor Grün (grün), Baronin von Porz
    (blau) und Professor Bloom (lila)
-   der Umschlag mit der Lösung muß nicht optisch dargestellt werden
-   die einzelnen Schritte auf dem Flur müssen nicht optisch dargestellt
    werden, es reicht eine geradlinige Bewegung (siehe Hilfe "Kreis
    bewegen") auf die Endposition. Trotzdem darf dabei natürlich kein
    ungültiger Zug erfolgen!
-   bei einer Verdächtigung zeigt nicht nur der linke Nachbar eine
    Karte, sondern ALLE anderen Mitspieler, falls sie eine passende
    haben. Es bleibt aber bei maximal einer Karte pro Mitspieler, die
    gezeigt wird
-   hat der menschliche Spieler eine falsche Anklage geäußert, endet das
    Spiel mit einer aussagekräftigen Meldung (und läuft nicht nur mit
    den Computergegnern weiter)

### Darstellung

Das Fenster teilt sich (abgesehen von ein paar Buttons bzw. einem Menü)
in zwei große Bereiche auf: Auf der linken Seite ist der Spielplan mit
den Räumen des Landhauses zu sehen. Dieser ist anklickbar und ermöglicht
somit die Steuerung des menschlichen Spielers. Zusätzlich zu den Räumen
sind hier die sechs Spielfiguren und die sechs Tatwaffen zu sehen, wobei
hier jeweils eine relativ simple Darstellung ausreicht (farbige Kreise
für die Spielfiguren, Textausgaben für die Tatwaffen).

Die rechte Fensterseite nimmt ein Bereich für die Notizen während des
Spiels ein. Hier kann der menschliche Spieler sich also eintragen (bzw.
dies geschieht zu Beginn teilweise schon automatisch bei der
Kartenvergabe), welche Karten er selbst hat, welche davon er schon (wie
oft bzw. wem) gezeigt hat und welche Karten er von anderen gesehen hat
oder bei anderen Spielern vermutet (siehe Abschnitt "Notizen" weiter
unten).

Es müssen von Euch nicht exakt dieselben Komponenten wie im
Beispielprogramm verwendet werden. Die Übersichtlichkeit und
Benutzbarkeit soll aber möglichst hoch sein, um dem Spieler den
Spielspaß nicht zu vermiesen :-)

Die Auswahl der zu zeigenden Karte bei einer Verdächtigung eines anderen
Spielers bzw. das Ergebnis bei einer eigenen Verdächtigung soll dem
menschlichen Spieler ebenfalls möglichst übersichtlich präsentiert
werden.

### Ablauf und Steuerung

Zu Beginn eines neuen Spiels wird vom Nutzer abgefragt, wie viele
Spieler teilnehmen sollen (3 bis 6). Dann wird zunächst einmal die
Lösung des Falles festgelegt, indem jeweils eine Person, eine Tatwaffe
und ein Raum zufällig aus allen Karten ausgewählt und in einen
"Umschlag" gesteckt werden. Die restlichen Karten werden dann ebenfalls
zufällig auf alle teilnehmenden Spieler verteilt. Dabei kann es
passieren, daß nicht alle Spieler gleich viele Karten erhalten. Der
Unterschied darf aber nie mehr als 1 betragen (es wird also reihum
verteilt, begonnen mit dem menschlichen Spieler). Ebenso kann es durch
den Zufall sein, daß ein Spieler nur Raum- und der andere nur
Personen-Karten bekommt.

Es beginnt danach immer der menschliche Spieler mit der Figur von
Fräulein Ming (rot). Für jeden Spieler (menschlich oder Computergegner)
wird automatisch der Würfel geworfen und das Resultat angezeigt (eine
Animation wie im Beispielprogramm ist nicht erforderlich). Der
menschliche Spieler klickt dann entweder eine Kachel des Flures oder
einen Raum an, Computerspieler werden von der KI gesteuert gesetzt. Kann
er die Kachel bzw. den Raum mit der gewürfelten Zahl erreichen, wird
seine Figur dorthin platziert. Andernfalls erscheint (nur beim
menschlichen Spieler) eine Meldung, daß dieser Zug nicht möglich ist.
Falls der Zug auf eine Flurkachel führte, ist direkt der nächste
(Computer-)Spieler am Zug. Endete der Zug in einem Raum, spricht der
Spieler eine Verdächtigung aus. Danach ist dann der nächste Spieler an
der Reihe.

Dieser Ablauf wird reihum solange fortgeführt, bis ein Spieler die
korrekte Lösung herausgefunden und geäußert hat ("Anklage") bzw. bis der
menschliche Spieler eine falsche Lösung geäußert hat. KI-Spieler sollen
nur anklagen, wenn sie sich absolut sicher sind, diese kann dann also
nicht verkehrt sein.

### Notizen

Dem menschlichen Spieler soll es ermöglicht werden, während des Spiels
umfangreiche Notizen anzulegen. Diese unterscheiden sich zwischen
Notizen zu ihm selbst und zu den anderen Spielern:

Zu sich selbst (also zu Fräulein Ming) soll er für alle 21 Karten
notieren können, ob es sich dabei um eine eigene Karte handelt, ob er
diese Karte bereits 1x, 2x, 3x oder mehr als 3x gezeigt hat oder ob
nichts davon zutrifft (also insgesamt 6 verschiedene Zustände).

Zu anderen Spielern soll er für alle 21 Karten notieren können, ob er
diese Karte vom jeweiligen Spieler gesehen hat, ob dieser Spieler sie
sicher nicht hat, ob diese Karte Teil eines Verdachtes ist (in vier
Varianten: A, B, C, D) oder ob nichts davon zutrifft (also insgesamt 7
verschiedene Zustände). Ein "Verdacht" meint dabei folgendes: Wenn der
menschliche Spieler "sieht", daß ein Computerspieler bei einer
Verdächtigung eine Karte zeigt, dann weiß er erstmal nur, daß es eine
von den drei verdächtigten Karten sein muß, aber nicht genau, welche.
Bei einer Verdächtigung "Frau Weiß, Seil, Salon" könnte also bei allen
drei Karten ein "Verdacht A" notiert werden (entsprechend bei einer
folgenden Verdächtigung dann Verdacht B usw.). Durch im weiteren Verlauf
gezeigte Karten können sich dann die Karten so eines Verdachtes dann
soweit reduzieren, daß klar wird, welche Karte der Computerspieler
ursprünglich gezeigt haben muß und diese kann dann von "Verdacht A" auf
"gesehen" gesetzt werden.

Eine mögliche Umsetzung der Notizen zeigt das Beispielprogramm. Andere
Implementationen mit anderen Komponenten sind natürlich auch zugelassen.
Denkt aber immer an eine einfache und intuitive Bedienbarkeit!

### Dateien

#### Initialisierungsdatei

Wer möchte, kann die Datei
[`cluedo.json`](https://intern.fh-wedel.de/fileadmin/mitarbeiter/klk/Programmierpraktikum/WS20_Cluedo/cluedo.json)
\[fehlerhafte Türen an Arbeits- und Speisezimmer am 8.10. korrigiert,
center des Arbeitszimmers am 7.1.21 korrigiert\] zur Initialisierung
seines Programmes nutzen. Sie enthält

-   alle Personen mit ihren Startpositionen
-   alle Räume mit einer mittigen Positionsangabe und allen
    Türpositionen
-   alle Waffen

Die Positionsangaben beginnen anders als im Beispielprogramm links oben
bei 0/0. Diese Datei darf nach eigenen Wünschen angepasst werden. Wird
sie verwendet, muß sie in die jar eingebunden werden, damit davon
ausgegangen werden kann, daß sie zur Laufzeit zur Verfügung steht und
korrekten Inhalt beherbergt.

#### Spielstandsdatei

Bevor der Nutzer seinen Zug tätigt (also nicht zwischen mehreren
Computerzügen), kann er den aktuellen Spielstand abspeichern oder einen
gespeicherten Spielstand laden. Beim Laden eines Spielstandes oder beim
Klick auf "Neues Spiel" soll zudem proaktiv eine Nachfrage erscheinen,
ob der aktuelle Spielstand vorab gespeichert werden soll.

Eine Spielstandsdatei hat die Dateiendung json und enthält folgende für
den Menschen lesbare Einträge:

-   die teilnehmenden Spieler
    -   Spielername
    -   \[erweitert am 23.9.\] Intelligenz ("dumm", "normal", "schlau")
    -   Aufenthaltsort (einer der Räume oder der Flur)
    -   genaue Position
    -   \[erweitert am 14.7.\] ob aktuell in den Raum gewünscht (durch
        Verdächtigung) -> darf im nächsten Zug bleiben und selbst
        Verdächtigung äußern
    -   Karten (Personenkarten, Räume, Waffen)
-   die Waffen
    -   Bezeichnung
    -   Aufenthaltsort (einer der Räume)
-   die Notizen \[erweitert am 23.9.\] jedes ~~des menschlichen~~
    Spielers
    -   jeweils zu jeder der 6 Personen
        -   für jede Person, jeden Raum und jede Waffe einer der Werte  
            -   bei der eigenen Person: "-", "eigene Karte", "1x
                gezeigt", "2x gezeigt","3x gezeigt",">3x gezeigt"
            -   bei den anderen: "-", "gesehen", "hat nicht", "Verdacht
                A", "Verdacht B", "Verdacht C", "Verdacht D"

Zum Testen und Vergleichen stellen wir Euch hier eine
[Beispieldatei](https://intern.fh-wedel.de/fileadmin/mitarbeiter/klk/Programmierpraktikum/WS20_Cluedo/Spielstand.json)
\[y-Position von Gatow korrigiert am 9.12.\] zur Verfügung, deren
Positionsangaben obiges Bild abbilden.

Die Angaben von Spielern, Räumen und Waffen erfolgen immer als
menschenlesbarer String, müssen also für die Verarbeitung im Programm
umgesetzt werden (zu Enums). Der Lesbarkeit ist auch zu schulden, daß
doppelte Angaben erfolgen, wie die Position eines Spielers mit
Raumangabe und konkreter Position.

Beim Laden einer Spielstandsdatei ist sicherzustellen, daß diese
vollständig und korrekt aufgebaut ist. Andernfalls soll eine
Fehlermeldung erscheinen und das Spiel wird mit dem vorherigen Zustand
fortgeführt.

### KI

Jeder der KI-Spieler soll einer von drei Stärken zugeordnet werden
können: dumm, normal und schlau.

Die "**dumme**" KI geht von der aktuellen Position aus immer zum
nähesten Raum, der noch offen ist (also bisher nicht als Tatraum
ausgeschlossen werden konnte). Falls der KI-Spieler sich aktuell in
einem Raum befindet, wird dieser nicht mit in die Betrachtung
einbezogen, welcher Raum der "näheste" ist. Ansonsten ist hier ein
Wegfindealgorithmus umzusetzen, der über den Flur (aber ohne
Geheimgänge) die Entfernungen zu anderen Räumen berechnet. Andere
Spieler, die auf dem Flur stehen, müssen dabei berücksichtigt werden.
Sie verlängern also ggf. den Weg bzw. machen ihn sogar unmöglich, wenn
sie direkt vor einer Tür stehen.  
Sollte nur noch ein einziger Raum "offen" sein, der Spieler befindet
sich darin und muß ihn verlassen, dann bewegt er sich zu einem
zufälligen anderen Raum.  
Wenn die "dumme" KI einen Verdacht äußert, werden die Personen dabei von
unten nach oben (angefangen mit Prof. Bloom) und die Waffen von oben
nach unten durchgegangen (angefangen mit der Pistole). Es werden
allerdings nur "offene" Karten verdächtigt, also solche, die die KI
weder selbst besitzt, noch bisher schon gesehen hat.  
Gleiches gilt, wenn die KI selbst Karten zeigen muß: Falls mehrere
möglich sind, wird auch hier von oben nach unten die erste genommen
(also eher Personen als Waffen und eher Waffen als Räume).

Die "**normale**" KI bewegt sich wie die "dumme" KI, berücksichtigt
dabei allerdings auch Geheimgänge.  
Bei einer Verdächtigung werden die Personen diesmal von oben nach unten
verdächtigt, um somit möglichst andere Spieler von ihrer aktuellen
Position "wegzuwünschen". Die Waffen werden weiterhin ebenfalls von oben
nach unten verdächtigt. Auch hier soll wieder nur nach noch "offenen"
Karten gefragt werden.  
Beim Zeigen von Karten sollen solche Karten bevorzugt gezeigt werden,
die man vorher bereits gezeigt hat - je häufiger, desto besser. Es muß
sich hier also pro Karte gemerkt werden, wie oft diese schon gezeigt
wurde. Stehen dann mehrere Karten zur Auswahl, nimmt man die bisher
schon am häufigsten gezeigte. Bei mehreren gleich häufigen gilt wieder
die Reihenfolge von oben nach unten.

Die "**schlaue**" KI bewegt sich genau wie die "normale" KI.  
Bei einer Verdächtigung ist sie die einzige KI-Stärke, die auch nach
eigenen Karten fragen kann, um das Zeigen bestimmter anderer Karten zu
erzwingen. Konkret soll dies angewandt werden, wenn die Person und die
Waffe schon festgelegt werden konnten und nur noch der Raum fehlt: In
diesem Fall soll die "schlaue" KI dann statt der tatsächlichen Tatperson
und -waffe soweit möglich nach eigenen Personen- und Waffenkarten fragen
(wieder von oben nach unten), um so die Mitspieler ggf. wegzuwünschen
und auch nicht zu offensichtlich zu machen, welches Wissen die KI schon
hat. Ansonsten verhält die KI sich wie die "normale" KI bei
Verdächtigungen. Es soll sich aber zusätzlich gemerkt werden, welche
Verdächtigungen die anderen Spieler ausgesprochen und von welchen
Spielern sie daraufhin Karten gesehen haben. So läßt sich später ggf.
auf einzelne Karten schließen, die dann "abgehakt" werden können.  
Beim Zeigen von Karten soll die "schlaue" KI sich merken, wem sie
bereits welche Karten gezeigt hat und diese wenn möglich erneut zeigen.
Falls das nicht möglich ist, verhält sie sich wie die "normale" KI.

Ein KI-Spieler gibt erst dann seine Anklage (Person, Waffe und Raum)
bekannt, wenn er sich komplett sicher ist. Dann wird dies über eine
Meldung dem menschlichen Spieler mitgeteilt (z.B. "Anklage: Oberst von
Gatow hat den Fall gelöst: Prof. Bloom, Seil, Veranda") und das Spiel
endet. Anklagen kann eine KI immer dann, wenn sie am Zug ist, also
entweder vor dem Ziehen oder nach einem geäußerten Verdacht.

Beachtet bei der KI, daß viele Routinen und Algorithmen bei allen drei
KI-Stärken wiederverwendet werden können, wenn man sinnvoll
modularisiert!  
Sollte sich eine Figur (menschlicher Spieler oder KI) gar nicht bewegen
können, weil alle umliegenden Flurfelder belegt oder alle Türen
blockiert sind, bleibt sie einfach stehen und darf dann - falls es ein
Raum ist - dort auch (erneut) eine Verdächtigung aussprechen.
