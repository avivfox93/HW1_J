import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class AddressBookIterator {
    private RandomAccessFile raf;
    private static final int RECORD_SIZE = (CommandButton.RECORD_SIZE * 2);
    public AddressBookIterator(RandomAccessFile raf) {
        this.raf = raf;
    }
    public ListIterator<Address> iterator() {
        return new ListIterator<>() {
            private long pos = 0;
            private long lastReturned = -1;
            private boolean next;

            @Override
            public boolean hasNext() {
                try {
                    return (raf.length() > this.pos);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }

            @Override
            public Address next() {
                try {
                    if (this.pos == raf.length()) throw new NoSuchElementException();
                    this.next = true;
                    raf.seek(this.pos);
                    this.pos += RECORD_SIZE;
                    this.lastReturned = raf.getFilePointer();
                    return new Address(raf);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }

            @Override
            public boolean hasPrevious() {
                return this.pos > RECORD_SIZE;
            }

            @Override
            public Address previous() throws NoSuchElementException {
                try {
                    this.next = false;
                    if (this.pos == 0) throw new NoSuchElementException();
                    long prev = this.pos - RECORD_SIZE;
                    this.lastReturned = prev;
                    raf.seek(prev);
                    Address a = new Address(raf);
                    this.pos -= RECORD_SIZE;
                    return a;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }

            @Override
            public int nextIndex() {
                return (int)(this.pos / RECORD_SIZE) + 1;
            }

            @Override
            public int previousIndex() {
                return (int)(pos / RECORD_SIZE) - 1;
            }

            @Override
            public void remove() throws IllegalStateException {
                try {
                    if (raf.length() == 0 || this.lastReturned == -1) throw new IllegalStateException();
                    int size = (int) (raf.length() - lastReturned);
                    long newSize = raf.length() - RECORD_SIZE;
                    byte[] arr = new byte[size];
                    if (!this.next) raf.seek(lastReturned + RECORD_SIZE);
                    else raf.seek(pos);
                    raf.read(arr);
                    raf.seek(this.lastReturned);
                    raf.write(arr);
                    raf.setLength(newSize);
                    this.pos = this.lastReturned;
                    this.lastReturned = -1;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void set(Address o) {
                try {
                    if (lastReturned == -1) throw new IllegalStateException();
                    raf.seek(this.lastReturned - RECORD_SIZE);
                    Address.writeToFile(o, raf);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void add(Address o) {
                try {
                    raf.seek(this.pos);
                    if (raf.length() == 0) {
                        Address.writeToFile(o, raf);
                        this.pos += RECORD_SIZE;
                        return;
                    }
                    byte[] arr = new byte[(int) (raf.length() - pos)];
                    raf.read(arr);
                    raf.seek(this.pos);
                    this.pos += RECORD_SIZE;
                    Address.writeToFile(o, raf);
                    raf.write(arr);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
}
class Address{
    private String name,street,city,state;
    private int zip;
    public Address(String name, String street, String city, int zip, String state){
        this.name = name;
        this.street = street;
        this.city = city;
        this.zip = zip;
        this.state = state;
    }
    public Address(RandomAccessFile raf)throws IOException{
         this.name =
                FixedLengthStringIO.readFixedLengthString(CommandButton.NAME_SIZE, raf);
        this.street =
                FixedLengthStringIO.readFixedLengthString(CommandButton.STREET_SIZE, raf);
        this.city =
                FixedLengthStringIO.readFixedLengthString(CommandButton.CITY_SIZE, raf);
        this.state =
                FixedLengthStringIO.readFixedLengthString(CommandButton.STATE_SIZE, raf);
        this.zip =
                Integer.valueOf(FixedLengthStringIO.readFixedLengthString(CommandButton.ZIP_SIZE, raf).trim());
    }
    public int getZip() {
        return this.zip;
    }
    public String getState() {
        return this.state;
    }
    public String getStreet() {
        return this.street;
    }
    public String getName() {
        return this.name;
    }
    public String getCity() {
        return this.city;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setState(String state) {
        this.state = state;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public void setZip(int zip) {
        this.zip = zip;
    }
    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public boolean equals(Object o){
		if(o == null) return false;
        if(o == this)return true;
        if(!(o instanceof Address))return false;
        Address a = (Address)o;
        return (a.getCity().equals(this.getCity()) &&
                a.getName().equals(this.getName()) &&
                a.getState().equals(this.getState()) &&
                a.getStreet().equals(this.getStreet()));
    }
    @Override
    public int hashCode(){
        return this.name.hashCode() + this.street.hashCode() + this.city.hashCode() + this.state.hashCode();
    }
    @Override
    public String toString(){
        return String.format("Name: %s, City: %s, State: %s, Street: %s, Zip: %s",this.name,this.city,this.state,this.street,this.zip);
    }
    public static void writeToFile(Address o, RandomAccessFile raf) throws IOException{
        FixedLengthStringIO.writeFixedLengthString(o.getName(),
                CommandButton.NAME_SIZE, raf);
        FixedLengthStringIO.writeFixedLengthString(o.getStreet(),
                CommandButton.STREET_SIZE, raf);
        FixedLengthStringIO.writeFixedLengthString(o.getCity(),
                CommandButton.CITY_SIZE, raf);
        FixedLengthStringIO.writeFixedLengthString(o.getState(),
                CommandButton.STATE_SIZE, raf);
        FixedLengthStringIO.writeFixedLengthString(Integer.toString(o.getZip()),
                CommandButton.ZIP_SIZE, raf);
    }
}