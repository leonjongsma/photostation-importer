package nl.leonjongsma.photostation.importer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.drew.metadata.Tag;

import static com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOM.indent;

public class Application {

    private static final String INPUT_FOLDER = "C:\\Users\\Leon\\Desktop\\export";
    private static final String OUTPUT_FOLDER = "W:\\";
    private static final String DONE_FOLDER = "C:\\importer\\done2";

    public static void main(String[] args) {

        try {
            List<File> filesInFolder = Files.walk(Paths.get(INPUT_FOLDER))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            filesInFolder.forEach(file -> {
                try {

                    Metadata metadata = ImageMetadataReader.readMetadata(file);
                    System.out.println(metadata.getDirectories().toString());
                    ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                    Date dateOriginal = null;
                    ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                    if (exifSubIFDDirectory != null) {
                        dateOriginal = exifSubIFDDirectory.getDateOriginal();
                    }
                    if (exifIFD0Directory != null) {
                        dateOriginal = exifIFD0Directory.getDate(ExifDirectoryBase.TAG_DATETIME);
                        System.out.println(dateOriginal);
                    }


                    if (dateOriginal != null) {

                        LocalDate localDate = dateOriginal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        String directoryStructure = createDirectoryStructureForDate(localDate);
                        String fullDirectoryStructure = OUTPUT_FOLDER + File.separator + directoryStructure;
                        createFolder(fullDirectoryStructure);
                        Path copied = Paths.get(fullDirectoryStructure + File.separator + file.getName());
                        Path originalPath = file.toPath();
                        Path target = Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);

                        if (target != null) {
                            System.out.println("file copied succesfull");
                            Path backupPath = Paths.get(DONE_FOLDER + File.separator + file.getName());
                            Files.move(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ImageProcessingException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createDirectoryStructureForDate(LocalDate localDate) {

        String year = String.valueOf(localDate.getYear());
        String month = String.valueOf(localDate.getMonth().getValue());
        String day = String.valueOf(localDate.getDayOfMonth());
        return year + File.separator + month + File.separator + day;
    }

    private static void createFolder(String folderPath) {

        // Check If Directory Already Exists Or Not?
        Path dirPathObj = Paths.get(folderPath);
        boolean dirExists = Files.exists(dirPathObj);
        if (dirExists) {
        } else {
            try {
                // Creating The New Directory Structure
                Files.createDirectories(dirPathObj);
                System.out.println("! New Directory Successfully Created !");
            } catch (IOException ioExceptionObj) {

                System.out.println("Problem Occured While Creating The Directory Structure= " + ioExceptionObj.getMessage());
            }
        }
    }
}
