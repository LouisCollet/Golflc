package utils;

/**
 *
 * @author collet
 * http://andreinc.net/tag/filevisitresult-skip_subtree/
 * The FileVisitor interface allows us to recursively traverse file structures – folders, sub-folders and files.

Every method of this interface can return 4 possible results (instances of the FileVisitResult enum):

    FileVisitResult.CONTINUE: This means that the traversal process will continue.
    FileVisitResult.SKIP_SIBLINGS: This means that the traversal process will continue without visiting the siblings (files or folders) of that particular Path
    FileVisitResult.SKIP_SUBTREE: This means that the traversal process will continue without visiting the rest of the tree entries.
    FileVisitResult.TERMINATE: This means that the traversal process should stop.

This FileVisitor interface has 4 methods:

    visitFile():
    The method is invoked for a file. The method should return a FileVisitResult.CONTINUE result 
    * or a FileVisitResult.TERMINATE result.
    * The method receive a reference to the file (a Path object) and
    * to the BasicFileAttributes object associated with the Path.
    * 
    preVisitDirectory():
    This method is invoked for a directory before visiting its children.
    * The method returns FileVisitResult.CONTINUE if we want it’s children to be visited
    * or FileVisitResult.SKIP_SUBTREE if we want the process to stop.
    * If we want to skip visiting the siblings of the directory we need to return FileVisitResult.SKIP_SIBLINGS .
    * 
    postVisitDirectory():
    This method is invoked after we visit all the children of a directory 
    * (including other folders and their descendants).
    * 
    visitFileFailed():
    This method is invoked if a file (or folder) cannot be accessed.

 */

 
import java.io.IOException;
import java.nio.file.*;
//import java.nio.file.FileVisitResult;
//import java.nio.file.FileVisitor;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.nio.file.attribute.*;
//import java.nio.file.attribute.AclFileAttributeView;
//import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
 
// Java NIO Delete Directory
 
public class FolderDeleteNIO {
 
   public static void removeRecursive(Path path) throws IOException
    {
        // TODO Auto-generated method stub
        //declaring the path to delete
    //    Path path = Paths.get("E:/tmp/java/tutorial/nio/file/delete");
   //     path = Paths.get("C:/tmpLC");
        System.out.println("Starting Deleting recursively : " + path);
        //browsing the file directory and delete recursively using java nio
        Files.walkFileTree(path, new SimpleFileVisitor<Path>()
{
  @Override
  //Invoked for a directory after entries in the directory, and all of their descendants,
  //have been visited. This method is also invoked when iteration of the directory completes prematurely
  //(by a visitFile method returning SKIP_SIBLINGS, or an I/O error when iterating over the directory).
  public FileVisitResult postVisitDirectory(Path dir, IOException IOexc)
                    throws IOException
  {
                // TODO Auto-generated method stub
      if (IOexc == null) {
                System.out.println("postVisitDirectory - Deleting directory :"+ dir);
                 Files.delete(dir);
                 return FileVisitResult.CONTINUE;
             } else {
                 // directory iteration failed
           System.out.println("postVisitDirectory - ERROR Deleting directory :"+ dir);
                 throw IOexc;
             }
      
        //        System.out.println("postVisitDirectory - Deleting directory :"+ dir);
        //        Files.delete(dir);
        //        return FileVisitResult.CONTINUE;
 }
 
 @Override
 // Invoked for a directory before entries in the directory are visited.
//If this method returns CONTINUE, then entries in the directory are visited.
//If this method returns SKIP_SUBTREE or SKIP_SIBLINGS then entries in the directory 
//(and any descendants) will not be visited.
 public FileVisitResult preVisitDirectory(Path dir,BasicFileAttributes attrs)
         throws IOException {
                // TODO Auto-generated method stub
                return FileVisitResult.CONTINUE;
            }
 
 @Override
 //Invoked for a file in a directory.
 public FileVisitResult visitFile(Path file,BasicFileAttributes attrs)
          throws IOException {
                // TODO Auto-generated method stub
     
   //  long fileSize = attrs.size() / 1024;
   // if (fileSize >= this.size) {  ...
     
     
     Path fileName = file.getFileName();
        if (fileName.endsWith("db.lck"))
        {
            String s = file.toAbsolutePath().toString();
            System.out.println("fileName endsWith db.lck: " + s);
            System.out.println(String.format("Visiting file '%s' which has size %d bytes", fileName, attrs.size()));
            FileStore fileStore = Files.getFileStore(file);
            System.out.println("Total space = " + fileStore.getTotalSpace());
            System.out.println("Unallocated space = " + fileStore.getUnallocatedSpace());
            System.out.println("Usable space = " + fileStore.getUsableSpace());
            AclFileAttributeView view = Files.getFileAttributeView(file, AclFileAttributeView.class);
		 if (view != null) {
			 List<AclEntry> acls = view.getAcl();
			 for(AclEntry acl: acls){
				 System.out.println("Attribute view : " + acl.principal().getName()+":"+acl.type());
			 }
		 }
              //   Files.
        }
                System.out.println("visitFile - Deleting file: "+file);
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
 
 @Override
 //Invoked for a file that could not be visited.
 //This method is invoked if the file's attributes could not be read,
 // the file is a directory that could not be opened, and other reasons.
 public FileVisitResult visitFileFailed(Path file, IOException exc)
                    throws IOException {
                // TODO Auto-generated method stub
                System.out.println("Error in visitFileFailed = \n    " + file + "\n    " + exc.toString());
                return FileVisitResult.SKIP_SUBTREE;
            //    return FileVisitResult.CONTINUE;
            }
 
        }); // ferme Files.walkFileTree !!
    } //end method removeRecursive
   
 public static void main(String[] args) throws IOException
{
  //  Path path = Paths.get("C:/tmpLC");C:\Users\collet\Documents\NetBeansProjects\EmbeddedGlassfish\embedded-samples
    Path p = Paths.get("C:\\Users\\collet\\Documents\\NetBeansProjects\\EmbeddedGlassfish\\embedded-samples");
    removeRecursive(p);
    
    Path path=Paths.get(System.getProperty("user.dir"),"embedded-samples");
    System.out.println("getProperty = " + path);
    
  //  Path homeFolder = Paths.get("C:\\");
//FileVisitor fileVisitor = new FileSizeVisitor(new Long(5000));
//try {
//	Files.walkFileTree(homeFolder, fileVisitor);
//} catch (IOException e) {
//	e.printStackTrace();
//}
  } // end main
} //end class
 