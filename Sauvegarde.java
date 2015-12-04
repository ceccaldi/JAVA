package Sauvegarde;


import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Sauvegarde{
	static int count = 0 ;
	static Hashtable<String, File> mapBackup = new Hashtable<String, File>();

	public static void main(String[] args) throws HashGenerationException{
		// Va contenir les fichiers sauvegardés	
		//Dossier BACKUPP
		String dirBackup = "/home/etudiants/csid/tceccaldi/Images/testbackup";

		//R�cup�re les fichiers du dossier Backup 
		File fileToBackup = new File(dirBackup);
		File[] filesBackup = fileToBackup.listFiles();

		//Dossier à copier
		String dirLive = "/home/etudiants/csid/tceccaldi/Images/test";

		//Récupère les fichiers du dossier live
		File fileToLive = new File(dirLive);
		File[] filesLive = fileToLive.listFiles();

		//Si Pas de fichier Live --> fin du prgm
		if (filesLive != null) {
			//Constitution mapBackup
			recupBackup(filesBackup);

			execCopy(filesLive,mapBackup, dirBackup, dirLive);

			System.out.println("Sauvegarde terminée.");
			System.out.println(count + " Fichier(s) copié(s)");

		}
		else{
			System.out.println("Le dossier live est vide");
		}
	}

	private static void recupBackup(File[]filesBackup) throws HashGenerationException {
		for (int i = 0; i < filesBackup.length; i++) {
			//Test Si le fichier est dans le dossier de backup
			if (filesBackup[i].isDirectory()) {
				recupBackup(filesBackup[i].listFiles());
			}
			else{
				mapBackup.put(generateSHA256(filesBackup[i]), filesBackup[i]); // R�cup�rer tous les fichier du dossier backup
			}
		}
	}

	private static void execCopy(File[]filesLive, Hashtable<String, File> mapBackup, String dirBackup, String dirLive) throws HashGenerationException {
		for (int i = 0; i < filesLive.length; i++) {
			//Test si c'est un sous dossier
			if (filesLive[i].isDirectory()) {
				//Création du sous-dossier dans le dossier de backup
				try {
					Process p;
					p = Runtime.getRuntime().exec("mkdir -p " + dirBackup+"/"+filesLive[i].getName());
					p.waitFor();
					//System.out.println(cmd);
					//System.out.println("1 Dossier a été créé");
				} 
				catch (Exception e) {
					e.printStackTrace();
				}

				execCopy(filesLive[i].listFiles(),mapBackup, dirBackup+"/"+filesLive[i].getName(), dirLive+"/"+filesLive[i].getName());
			}
			else{
				//Test Si le fichier est dans le dossier de backup
				if(!mapBackup.containsKey(generateSHA256(filesLive[i]))){
					//Ajoute le fichier copié 
					mapBackup.put(generateSHA256(filesLive[i]), filesLive[i]);
					//Copier le fichier dans dossier backup
					String command = "cp "+ dirLive + "/" + filesLive[i].getName() + " " + dirBackup; //cmd shell 
					//Execute la commande
					Process p;
					try {
						p = Runtime.getRuntime().exec(command);
						p.waitFor();
						System.out.println(command);
						System.out.println("1 Fichier a été copié.");
						count ++ ;
					} 
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}


	// M�thode pour Hashage
	private static String hashFile(File file, String algorithm) throws HashGenerationException {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			MessageDigest digest = MessageDigest.getInstance(algorithm);

			byte[] bytesBuffer = new byte[1024];
			int bytesRead = -1;

			while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
				digest.update(bytesBuffer, 0, bytesRead);
			}

			byte[] hashedBytes = digest.digest();

			return convertByteArrayToHexString(hashedBytes);
		} catch (NoSuchAlgorithmException | IOException ex) {
			return "Erreur";
		}
	}

	private static String convertByteArrayToHexString(byte[] arrayBytes) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < arrayBytes.length; i++) {
			stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return stringBuffer.toString();
	}

	//utiliser le hash
	public static String generateMD5(File file) throws HashGenerationException {
		return hashFile(file, "MD5");
	}
	public static String generateSHA256(File file) throws HashGenerationException {
		return hashFile(file, "SHA-256");
	}
}