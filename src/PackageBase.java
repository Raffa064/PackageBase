import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import PackageBase.Entry;
import java.util.logging.Level;

public class PackageBase {
	private File file;
	private Metadata metadata;
	private List<Entry> entries;

	public PackageBase(File file) {
		this.file = file;
		load();
	}

	public Entry add(String name, String packageName) {
		Entry entry = getEntryByPackage(packageName);
		
		if (entry != null) {
			return entry;	
		}
		
		entry = new Entry();
		entry.setId(metadata.idCounter++);
		entry.setName(name);
		entry.setPackageName(packageName);

		if (metadata.minWipedIndex < 0) {
			entries.add(entry);
		} else {
			metadata.wipedCounter--;
			entries.set(metadata.minWipedIndex, entry);
			updateMinWipedIndex();
		}

		return entry;
	}

	public List<Entry> query(String queryString) {
		List<Entry> matchedEntries = new ArrayList<>();

		char[] queryChars = queryString.toCharArray();
		for (Entry entry : entries) {
			if (entry.wiped) continue;

			if (entry.match(queryChars)) {
				matchedEntries.add(entry);
			}
		}

		return matchedEntries;
	}

	public Entry getEntryById(long id) {
		for (Entry entry : entries) {
			if (entry.wiped) continue;

			if (entry.id == id) {
				return entry;
			}
		}

		return null;
	}

	public Entry getEntryByName(String name) {
		for (Entry entry : entries) {
			if (entry.wiped) continue;

			if (entry.getNameString().equals(name)) {
				return entry;
			}
		}

		return null;
	}

	public Entry getEntryByPackage(String packageName) {
		for (Entry entry : entries) {
			if (entry.wiped) continue;

			if (entry.getPackageNameString().equals(packageName)) {
				return entry;
			}
		}

		return null;
	}

	public void delete(Entry entry) {
		entry.wiped = true;

		metadata.wipedCounter++;

		int entryIndex = entries.indexOf(entry);
		if (metadata.minWipedIndex == -1 || metadata.minWipedIndex > entryIndex) {
			metadata.minWipedIndex = entryIndex;
		}
	}

	public void clearTrash() {
		metadata.wipedCounter = 0;
		metadata.minWipedIndex = -1;
		for (int i = 0; i < entries.size(); i++) {
			Entry entry = entries.get(i);

			if (entry.wiped) {
				entries.remove(i);
				i--;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(metadata.toString());

		for (Entry entry : entries) {
			builder.append("\n");
			builder.append(entry.toString());
		}

		return builder.toString();
	}

	public void load() {
		if (file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);

				byte[] buffer = new byte[100];
				fis.read(buffer);

				metadata = new Metadata(buffer);
				entries = new ArrayList<>();
				
				buffer = new byte[719];
				while (fis.read(buffer) > 0) {
					Entry entry = new Entry(buffer);
					entries.add(entry);
				}

				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}	
		} else {
			metadata = new Metadata();
			entries = new ArrayList<>();

			save();
		}
	}

	public void save() {
		try {
			FileOutputStream fos = new FileOutputStream(file);

			fos.write(metadata.getBytes());

			for (Entry entry : entries) {
				fos.write(entry.getBytes());
			}

			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateMinWipedIndex() {
		for (int i = metadata.minWipedIndex; i < entries.size(); i++) {
			if (entries.get(i).wiped) {
				metadata.minWipedIndex = i;
				return;
			}
		}

		metadata.wipedCounter = 0;
		metadata.minWipedIndex = -1;
	}

	public static class Metadata {
		private final char[] magicNumbers = { 'p', 'k', 'b' };
		private int version = 1;
		private long idCounter;
		private int wipedCounter; // Wiped entry counter
		private int minWipedIndex = -1; // Minimum wiped index

		public Metadata() {}

		public Metadata(byte[] buffer) {
			ByteBuffer bf = ByteBuffer.wrap(buffer);

			for (int i = 0; i < magicNumbers.length; i++) {
				if (bf.getChar() != magicNumbers[i]) {
					throw new Error("Invalid magic numbers");
				}
			}

			version = bf.getInt();
			idCounter = bf.getLong();
			wipedCounter = bf.getInt();
			minWipedIndex = bf.getInt();
		}


		public byte[] getBytes() {
			ByteBuffer bf = ByteBuffer.allocate(100);
			
			for (int i = 0; i < magicNumbers.length; i++) {
				bf.putChar(magicNumbers[i]);
			}
			
			bf.putInt(version);
			bf.putLong(idCounter);
			bf.putInt(wipedCounter);
			bf.putInt(minWipedIndex);

			return bf.array();
		}

		@Override
		public String toString() {
			return "PackageBase: version=" + version + ", idCounter=" + idCounter + ", wipedCounter=" + wipedCounter + ", minWipedIndex=" + minWipedIndex;
		} 
	}

	public static class Entry {
		private boolean wiped;
		private long id;
		private char[] name = new char[100];
		private char[] packageName = new char[255];

		public Entry() {}

		public Entry(byte[] buffer) {
			ByteBuffer bf = ByteBuffer.wrap(buffer);

			wiped = (bf.get() & 0b1) == 1;
			id = bf.getLong();

			for (int i = 0; i < 100; i++) {
				name[i] = bf.getChar();
			}

			for (int i = 0; i < 255; i++) {
				packageName[i] = bf.getChar();
			}
		}

		public void setId(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

		public void setName(char[] name) {
			copyCharArray(name, this.name);
		}

		public void setName(String name) {
			setName(name.toCharArray());
		}

		public char[] getName() {
			return name;
		}
		
		public String getNameString() {
			return charArrayToString(name);
		}
		
		public void setPackageName(char[] packageName) {
			copyCharArray(packageName, this.packageName);
		}

		public void setPackageName(String packageName) {
			setPackageName(packageName.toCharArray());
		}

		public char[] getPackageName() {
			return packageName;
		}
		
		public String getPackageNameString() {
			return charArrayToString(packageName);
		}

		public boolean match(char[] query) {
			if (query.length > 355) {
				return false;
			}

			for (int i = 0; i < 355 - query.length; i++) {
				boolean matched = true;
				for (int j = 0; j < query.length; j++) {
					int k = i + j;
					char sourceChar = k < 100 ? name[k] : packageName[k - 100]; // Switch between arrays
					char queryChar = query[j];

					if (sourceChar != queryChar) {
						matched = false;
						break;
					}
				}

				if (matched) {
					return true; 
				}	
			}

			return false;
		}
		
		public byte[] getBytes() {
			ByteBuffer bf = ByteBuffer.allocate(719);
			
			bf.put((byte) (wiped? 0b1 : 0b0));
			bf.putLong(id);

			for (int i = 0; i < 100; i++) {
				bf.putChar(name[i]);
			}
			
			for (int i = 0; i < 255; i++) {
				bf.putChar(packageName[i]);
			}
			
			return bf.array();
		}

		@Override
		public String toString() {
			String wipeState = wiped ? "WIP" : "ACT";
			return wipeState + ": id=" + id + ", name=" + getNameString() + ", package=" + getPackageNameString();
		}

		private void copyCharArray(char[] source, char[] target) {
			int limitedLength = Math.min(source.length, target.length);
			
			for (int i = 0; i < limitedLength; i++) {
				target[i] = source[i];
			}

			if (limitedLength < target.length) {
				target[limitedLength] = 0x0; // End sequence char
			}
		}
		
		private String charArrayToString(char[] array) {
			StringBuilder builder = new StringBuilder(array.length);

			for (int i = 0; i < array.length; i++) {
				char c = array[i];

				if (c == 0x0) {
					break;
				}

				builder.append(c);
			}

			return builder.toString();
		}
	}
}
