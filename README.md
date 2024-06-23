## Gran Premio MIVIA 2024

Contest tra i veicoli di ciascun gruppo del corso di “*Intelligenza Artificiale: metodi ed applicazioni*” i quali dovranno essere in grado di completare, in maniera autonoma, il giro della pista nel minor tempo possibile.

### Allegati

In allegato sono forniti quattro (4) documenti:

1. **Report breve**, nel quale sono riportate le scelte progettuali, le decisioni tecniche e le motivazioni dietro di esse;
2. **Codice**, o meglio tutti i codici che sono necessari per la corretta esecuzione del progetto. Per favorirne una maggiore comprensione e leggibilità si è provveduto ad aggiungere i commenti;
3. **Presentazione**, in formato PowerPoint come complemento al report breve. In esso sono illustrate sinteticamente le scelte progettuali e i risultati ottenuti;
4. **Video del sistema in azione** per dimostrare il funzionamento, utile soprattutto per garantire la comprensione di aspetti complessi o difficili da esporre nella documentazione.

### Istruzioni compilazione ed esecuzione

Per compilare il codice fornito è sufficiente accedere alla directory src, e digitare il comando
 `javac -d ../classes scr/*.java`.

Per lanciare il client Java bisogna posizionarsi nella directory /classes e digitare nella shell il comando `java scr.Client scr.SimpleDriver host:localhost port:3001 verbose:on` il quale provvederà ad eseguire il codice. 
A questo punto è possibile avviare Torcs e configurare una nuova gara con giocatore *scr_server1,* iniziata la gara l’auto guiderà autonomamente.

### Strutturazione del codice sorgente

- **Action:** Permette la creazione dell’oggetto da restituire contenente i parametri dell’azione da intraprendere.
- **Classificatore:** permette di classificare un sample passato come parametro al metodo *classifica()*  e stampa la classe predetta su un file di output.
- **Client:** La classe consente la comunicazione con il server tramite una serie di parametri.
- **ContinuousCharReaderUI:** permette la lettura dei tasti premuti e rilasciati da tastiera durante la fase di creazione del dataset e la creazione di valori dell’action da compiere di conseguenza ai tasti premuti/rilasciati.
- **Controller:**  classe astratta che presenta i metodi principali da implementare al fine di realizzare un agente, è estesa da SimpleDriver.
- **KDTree:** Implementazione efficiente di un KDTree usato per il classificatore KNN.
- **MessageBasedSensorModel:** La classe implementa l’interfaccia SensorModel, ed ha il compito di rendere disponibili i sensori della telemetria fornita dal simulatore e ricevuti attraverso la socket, in altre parole fornisce dei metodi per ottenere lo stato corrente. I parametri sono disponibili con le funzioni get.
- **MessageParser:** La classe implementa la funzione per interpretare i messaggi ricevuti dal server e converte il messaggio testuale ricevuto in un hashtable che rappresenta lo stato del sistema.
- **NearestNeighbor:** Implementa un classificatore di tipo KNN, in particolare costruisce un kd-tree con i dati forniti nel dataset e fornisce un metodo per classificare un punto tramite il confronto con i k punti più vicini.
- **Sample:** Classe che rappresenta i punti nello spazio degli stati, ogni punto è costituito dalle features scelte. La classe inoltre fornisce un metodo per calcolare la distanza euclidea tra due punti.
- **SensorModel:** interfaccia che contiene i prototipi delle funzioni da  implementare per rendere disponibili i valori della telemetria ricevuta (sensori), è implementata da MessageBasedSensorModel.
- **SimpleDriver:** La classe utilizza i dati acquisiti dai sensori della telemetria e restituisce le azioni da intraprendere per la guida dell’auto, implementando i metodi presenti in Controller. In particolare sono fornite due implementazioni del metodo control: una da usare durante la fase di creazione del dataset e un’altra da usare durante la fase operativa, in entrambi i casi i dati sono normalizzati per una realizzazione ottimale della classificazione.
- **SocketHandler:** Istanzia la comunicazione tramite Socket con il server.

### Requisiti

- Java Development Kit (JDK) 17 o superiore
- TORCS installato

### Autore

**GRUPPO 25**

Apicella Antonio 0612705327 

Borrelli Simone 0612706355

Celano Benedetta Pia 0612705326

Grimaldi Vincenzo 0612705444

**RESPONSABILE:** Apicella Antonio