package FileSharing;

/**
* FileSharing/FileSharingServiceHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from filesharing.idl
* Monday, March 24, 2025 5:56:33 o'clock PM PDT
*/

public final class FileSharingServiceHolder implements org.omg.CORBA.portable.Streamable
{
  public FileSharing.FileSharingService value = null;

  public FileSharingServiceHolder ()
  {
  }

  public FileSharingServiceHolder (FileSharing.FileSharingService initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = FileSharing.FileSharingServiceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    FileSharing.FileSharingServiceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return FileSharing.FileSharingServiceHelper.type ();
  }

}
