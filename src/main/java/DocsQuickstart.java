import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.docs.v1.model.ParagraphElement;
import com.google.api.services.docs.v1.model.StructuralElement;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.File;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/* class to demonstrate use of Docs get documents API */
public class DocsQuickstart {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Docs API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String DOCUMENT_ID = "195j9eDD3ccgjQRttHhJPymLJUCOUjs-jmwTrekvdjFE";

    private static final List<String> SCOPES = Arrays.asList(
            DocsScopes.DOCUMENTS_READONLY,
            DriveScopes.DRIVE
    );

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */ //do docsów, wyzej do drivea
//    private static final List<String> SCOPES =
//            Collections.singletonList(DocsScopes.DOCUMENTS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/home/agata/Documents/projektMiasi/src/main/resources/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.

        java.io.File credentialsFile = new java.io.File("/home/agata/Documents/projektMiasi/src/main/resources/credentials.json");
        if (!credentialsFile.exists()) {
            throw new FileNotFoundException("Resource not found: " + credentialsFile.getAbsolutePath());
        }

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(credentialsFile)));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Docs service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();

        // Prints the title of the requested doc:
        // https://docs.google.com/document/d/195j9eDD3ccgjQRttHhJPymLJUCOUjs-jmwTrekvdjFE/edit
//        Document response = service.documents().get(DOCUMENT_ID).execute();
//        String title = response.getTitle();
//
//        System.out.printf("The title of the doc is: %s\n", title);

        // Fetch the document.
//        Document response = service.documents().get(DOCUMENT_ID).execute();
//
//        // Extract document title and content
//        String title = response.getTitle();
//        StringBuilder documentContent = new StringBuilder();
//
//        // Iterate through the document body and append text content.
//        for (StructuralElement element : response.getBody().getContent()) {
//            if (element.getParagraph() != null) {
//                // Loop through the paragraph elements
//                for (ParagraphElement paragraphElement : element.getParagraph().getElements()) {
//                    if (paragraphElement.getTextRun() != null) {
//                        documentContent.append(paragraphElement.getTextRun().getContent());
//                    }
//                }
//            }
//        }
//
//        // Print the title
//        System.out.printf("The title of the doc is: %s\n", title);
//
//        // Write the content to a local file (e.g., document.txt)
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter("document.txt"))) {
//            writer.write("Title: " + title + "\n\n");
//            writer.write(documentContent.toString());
//        }
//
//        System.out.println("Document saved locally as 'document.txt'");


        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // List files in the user's Google Drive
        FileList result = service.files().list()
                .setQ("mimeType='application/vnd.google-apps.document'") // Filtrujemy tylko dokumenty Google Docs
                .setPageSize(10) // Pobrane tylko 10 plików
                .setFields("nextPageToken, files(id, name)") // Pole "id" i "name"
                .execute();
        List<com.google.api.services.drive.model.File> files = result.getFiles();

        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            int i = 0;
            for (com.google.api.services.drive.model.File file : files) {
                System.out.printf("Found file: %s (ID: %s)\n", file.getName(), file.getId());
                if (i == 2) {
                    String fileId = files.get(2).getId(); // ID trzeciego pliku
                    System.out.println("ID trzeciego pliku: " + fileId);

                    // Następnie możesz użyć tego ID do pobrania i zapisania pliku lokalnie
                    downloadFile(fileId, service);
                }
                i++;

            }
        }

    }

    private static void downloadFile(String fileId, Drive service) {
        try {
            // Pobieranie pliku z Google Drive za pomocą jego ID
            File file = service.files().get(fileId).execute();
            System.out.println("Plik pobrany: " + file.getName());

            // Eksportowanie pliku Google Docs do formatu DOCX (lub innego)
            HttpResponse response = service.files()
                    .export(fileId, "application/vnd.openxmlformats-officedocument.wordprocessingml.document") // Eksport do DOCX
                    .executeMedia();
            InputStream inputStream = response.getContent();

            // Zapisanie pliku lokalnie w bieżącym katalogu roboczym
            OutputStream outputStream = new FileOutputStream(file.getName() + "2" + ".docx"); // Dodanie rozszerzenia .docx

            byte[] buffer = new byte[1024];
            int bytesRead;

            // Kopiowanie danych z InputStream do OutputStream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Zamknięcie strumieni
            inputStream.close();
            outputStream.close();

            System.out.println("Plik zapisany lokalnie: " + file.getName() + ".docx");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}