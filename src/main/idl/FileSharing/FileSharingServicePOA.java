package FileSharing;


/**
* FileSharing/FileSharingServicePOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from filesharing.idl
* Monday, March 24, 2025 5:57:12 o'clock PM PDT
*/

public abstract class FileSharingServicePOA extends org.omg.PortableServer.Servant
 implements FileSharing.FileSharingServiceOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("registerFile", new java.lang.Integer (0));
    _methods.put ("removeFile", new java.lang.Integer (1));
    _methods.put ("searchFile", new java.lang.Integer (2));
    _methods.put ("getFileOwner", new java.lang.Integer (3));
    _methods.put ("getFileInfo", new java.lang.Integer (4));
    _methods.put ("downloadFile", new java.lang.Integer (5));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // FileSharing/FileSharingService/registerFile
       {
         String fileName = in.read_string ();
         int userId = in.read_long ();
         this.registerFile (fileName, userId);
         out = $rh.createReply();
         break;
       }

       case 1:  // FileSharing/FileSharingService/removeFile
       {
         String fileName = in.read_string ();
         int userId = in.read_long ();
         this.removeFile (fileName, userId);
         out = $rh.createReply();
         break;
       }

       case 2:  // FileSharing/FileSharingService/searchFile
       {
         String fileName = in.read_string ();
         String $result[] = null;
         $result = this.searchFile (fileName);
         out = $rh.createReply();
         FileSharing.StringListHelper.write (out, $result);
         break;
       }

       case 3:  // FileSharing/FileSharingService/getFileOwner
       {
         String fileName = in.read_string ();
         String $result = null;
         $result = this.getFileOwner (fileName);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 4:  // FileSharing/FileSharingService/getFileInfo
       {
         int fileId = in.read_long ();
         String $result = null;
         $result = this.getFileInfo (fileId);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // FileSharing/FileSharingService/downloadFile
       {
         String fileName = in.read_string ();
         String $result = null;
         $result = this.downloadFile (fileName);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:FileSharing/FileSharingService:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public FileSharingService _this() 
  {
    return FileSharingServiceHelper.narrow(
    super._this_object());
  }

  public FileSharingService _this(org.omg.CORBA.ORB orb) 
  {
    return FileSharingServiceHelper.narrow(
    super._this_object(orb));
  }


} // class FileSharingServicePOA
