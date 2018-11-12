import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class AddressBookIterator implements ListIterator<Address> {
    private RandomAccessFile raf;
    private long lastReturned = -1;
    private boolean next;
    private static final int RECORD_SIZE = (CommandButton.RECORD_SIZE*2);
    public AddressBookIterator(RandomAccessFile raf){
        this.raf = raf;
    }
    @Override
    public boolean hasNext() {
        try {
            return (raf.length() > raf.getFilePointer());
        }catch (IOException ex){
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public Address next() {
        try {
            if(this.raf.getFilePointer() == this.raf.length()) throw new NoSuchElementException();
            this.next = true;
            this.lastReturned = this.raf.getFilePointer();
            return new Address(this.raf);
        }catch(IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean hasPrevious() {
        try {
            return raf.getFilePointer() > 0;
        }catch(IOException ex){
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public Address previous() throws NoSuchElementException {
        try {
            this.next = false;
            if(this.raf.getFilePointer() == 0) throw new NoSuchElementException();
            long prev = this.raf.getFilePointer() - RECORD_SIZE;
            this.lastReturned = prev;
            this.raf.seek(prev);
            Address a = new Address(raf);
            this.raf.seek(prev);
            return a;
        }catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public int nextIndex() {
        try {
            return (int) this.raf.getFilePointer()/RECORD_SIZE + 1;
        }catch (IOException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    @Override
    public int previousIndex() {
        try {
            if(this.raf.getFilePointer() == 0) throw new IOException();
            return (int) this.raf.getFilePointer()/RECORD_SIZE - 1;
        }catch (IOException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    @Override
    public void remove() throws IllegalStateException {
        try {
            if(this.raf.length() == 0 || this.lastReturned == -1)throw new IllegalStateException();
            int size = (int)(this.raf.length() - lastReturned);
            long newSize = this.raf.length() - RECORD_SIZE;
            byte[] arr = new byte[size];
            if(!this.next)this.raf.seek(lastReturned + RECORD_SIZE);
            this.raf.read(arr);
            this.raf.seek(this.lastReturned);
            this.raf.write(arr);
            this.raf.setLength(newSize);
            this.raf.seek(this.lastReturned);
            this.lastReturned = -1;
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void set(Address o) {
        try{
            this.raf.seek(this.raf.getFilePointer() - RECORD_SIZE);
            Address.writeToFile(o,raf);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void add(Address o) {
        try{
            long pos = this.raf.getFilePointer();
            if(this.raf.length() == 0){
                Address.writeToFile(o,this.raf);
                this.raf.seek(pos);
                return;
            }
            byte[] arr = new byte[(int)(this.raf.length() - pos)];
            this.raf.read(arr);
            this.raf.seek(pos);
            Address.writeToFile(o,this.raf);
            this.raf.write(arr);
            this.raf.seek(pos);
        }catch (IOException ex){
            ex.printStackTrace();
        }
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
        return this.name.hashCode() + this.street.hashCode() + this.city.hashCode() + this.state.hashCode() + 31*getZip();
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