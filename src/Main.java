import PackageBase.Entry;
import java.io.File;
import java.util.List;


public class Main {
	
	public static void main(String[] args) {
		String DATABASE_PATH = "/storage/emulated/0/AppProjects/PackageBase/src/db.pkb"; // Change it to your own location
		File f = new File(DATABASE_PATH);
		
		// it will load/create the database file
		PackageBase pkb = new PackageBase(f);
		
		// You can't add the same package mutiple times, it will return the same entry every time
		Entry entryA = pkb.add("DeepNote", "com.raffa064.deepnote");
		Entry entryB = pkb.add("DeepNote", "com.raffa064.deepnote");
		
		if (entryA == entryB) {
			System.out.println("Entries A and B is the same entry!");
		}
		
		// You can modify any entry value (will change entryB too, because they are references of the same object)
		entryA.setId(2364); // Not recommend to change it!
		entryA.setName("Other name");
		entryA.setPackageName("com.example.example");
		
		// Find/get entries
		List<Entry> entries = pkb.query("deep"); // Find all entrie wich contains deep in the name or package name
		
		Entry byId = pkb.getEntryById(2364);
		Entry byName = pkb.getEntryByName("Other Name");
		Entry byPackage = pkb.getEntryByPackage("com.raffa064.deepnote");
		
		// You can delete an entry, but it will continue existing in the memory, waiting to be replaced
		pkb.delete(entryA);
		
		// Remove all deleted entries from the memory
		pkb.clearTrash();
		
		// Store all data into the database file
		pkb.save();
		
		System.out.println(pkb);
    }
    
}
