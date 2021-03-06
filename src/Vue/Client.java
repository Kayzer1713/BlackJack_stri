package Vue;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client{

	private Socket requestSocket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String message;
	@SuppressWarnings("unused")
	private String pseudo;
	private Scanner sc;

	Client(){
		sc = new Scanner(System.in);
	}

	void run()
	{
		try{
			requestSocket = new Socket("localhost", 9999);
			System.out.println("Connected to localhost in port 9999");

			// Ouverture des connections
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			try {
				message = (String)in.readObject();
				System.out.println("Re�u>" + message);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			// Boucle principale de communication
			do{
				message = attenteMessage();
				switch(message) {
				case "new":
					premiereConnection();
					break;
				case "majTable":
					System.out.println("Re�u>maj Table");
					afficheTable();
					break;
				case "hit":
					System.out.println("Re�u>tire carte");
					break;
				case "again":
					System.out.println("Re�u>nouv partie");
					break;
				case "choixTable":
					System.out.println("Re�u>"+message);
					choisirTable();
					break;
				default :
					System.out.println("Re�u>Message inconnu! :"+message);
				}
			}while(!message.equals("STOP"));
			System.out.println(message + ": OK");
			System.out.println("Connexion ferm�");

		} catch(UnknownHostException unknownHost){
			System.err.println("vous essay� de vous connect� � un hote inconnu!");
		} catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			// Fermeture de la connexion
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}

	/**
	 * M�thode envoie message
	 * @param msg
	 */
	private void envoiMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("Envoi>" + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	private String attenteMessage() {
		System.out.println("Attente de r�ponse du serveur...");
		String re�u = null;
		long timeout = 5000;
		long tempsActuel = System.currentTimeMillis();
		try {
			do {
				re�u = (String)in.readObject();
			}	while( ( System.currentTimeMillis()-tempsActuel < timeout ) && re�u.equals("") );
			if ( re�u == null )
				throw new Exception("Timeout: le message n'as pas �tait re�us � temps ou le client ne r�pond plus...");
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return re�u;
	}

	private void premiereConnection() {
		
		System.out.println("Bonjour veuillez saisir un pseudo :");
		String str = sc.nextLine();
		System.out.println("Vous avez saisi : " + str);
		envoiMessage(str);
		message = attenteMessage();
		
		while ( message.equals("pseudoDejaExistant") ) {
			System.out.println("Erreur: pseudo d�j� existant veuillez essayer autre chose...");
			str = sc.nextLine();
			envoiMessage(str);
			message = attenteMessage();
		}
		
		if (message.equals("valide")) {
			System.out.println("Vous �tes maintenant connect� sour le pseudo:" + str);
			this.pseudo = str;
		} else {
			envoiMessage("STOP");
		}
	}

	private String afficheTable() {
		System.out.println("affichage des tables");
		return "not implemented yet";
	}

	/**
	 * M�thode du choix de la table
	 */
	private void choisirTable() {
		
		//Affichage des tables
		message = attenteMessage();
		System.out.println("Voici la liste des tables : " + message);
		System.out.println("Veuillez selectionner une table:");
		
		//Envoi du choix de la table
		String str = sc.nextLine();
		envoiMessage(str);
		message = attenteMessage();
		
		while (message.equals("errNum")) {
			System.out.println("Veuillez saisir un num�ro de table valide!");
			str = sc.nextLine();
			envoiMessage(str);
			message = attenteMessage();
		}
		//message = attenteMessage();
		
		if(message.equals("valide")) {
			System.out.println("vous avez bien rejoins la table " + str);	
		}else {
			message = attenteMessage();
			if (message.equals("miseTable")) {
				System.out.println("Veuillez saisir une mise min pour votre table:");
				str = sc.nextLine();
				envoiMessage(str);
				message = attenteMessage();
				
				while (message.equals("miseIncorrecte")) {
					System.out.println("Veuillez entrer une mise min dans vos moyens et sup�rieure � 0!");
					str = sc.nextLine();
					envoiMessage(str);
					message = attenteMessage();
				}
				
				if (message.equals("nbJoueurMax")) {
					System.out.println("Veuillez saisir le nombre de joueur max pour la table:");
					str = sc.nextLine();
					envoiMessage(str);
					message = attenteMessage();
					
					while (message.equals("nbJoueurIncorrecte")) {
						System.out.println("Veuillez entrer un nombre de joueur sup�rieure � 2 (Dealer + vous)!");
						str = sc.nextLine();
						envoiMessage(str);
						message = attenteMessage();
					}

					// la table est valid�e:
					System.out.println("La table cr�e avec succ�s:");
					System.out.println(attenteMessage());
					if(attenteMessage().equals("valide")) {
						System.out.println("Vous avez rejoins avec succ�s votre table.");
					}
				}
			} else {
				envoiMessage("STOP");
			}
		}
		envoiMessage("STOP");
		sc.close();
	}
	
	
	public static void main(String args[]) {
		Client client = new Client();		
		client.run();
	}
}