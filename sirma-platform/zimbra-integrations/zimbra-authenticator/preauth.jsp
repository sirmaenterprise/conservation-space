<!--

To configure:

1) Generate a preauth domain key for your domain using zmprov:

zmprov gdpak domain.com
preAuthKey:  ee0e096155314d474c8a8ba0c941e9382bb107cc035c7a24838b79271e32d7b0

Take that value, and set it below as the value of DOMAIN_KEY

2) restart server (only needed the first time you generate the domain pre-auth key)

3) redirect users to this (this, as in *this* file after you install it) JSP page:

http://server/zimbra/webapps/preauth.jsp

And it will construct the preauth URL

-->
<%@ page import="java.security.MessageDigest" %>
<%@ page import="java.security.InvalidKeyException" %>
<%@ page import="java.security.NoSuchAlgorithmException" %>
<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="javax.xml.bind.DatatypeConverter" %>
<%@ page import="java.security.SecureRandom" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="javax.crypto.Mac" %>
<%@ page import="javax.crypto.SecretKey" %>
<%!

	public static String generateRedirect(HttpServletRequest request) {
	    String redirectUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

	    String account = request.getParameter("account");
	    String domainKey = generateDomainKey(account);
	    String view = request.getParameter("preferredView");
	    String skin = request.getParameter("preferredSkin");

		if (account != null) {
	        HashMap params = new HashMap();
	        String ts = System.currentTimeMillis() + "";
	        params.put("account", account);
	        params.put("by", "name"); // needs to be part of hmac
	        params.put("timestamp", ts);
	        params.put("expires", "0"); // means use the default
	        String preAuth = computePreAuth(params, domainKey);
	        redirectUrl += "/service/preauth/?" +
	            "account=" + account +
	            "&by=name" +
	            "&timestamp=" + ts +
	            "&expires=0" +
	            "&preauth=" + preAuth;

			if(view != null){
				redirectUrl += "&redirectURL=/?view="+view;
			}

	    	if(skin != null) {
				redirectUrl += "&skin="+skin;
			}
	    }
		return redirectUrl;
	}
	
    public static  String computePreAuth(Map params, String key) {
        TreeSet names = new TreeSet(params.keySet());
        StringBuffer sb = new StringBuffer();
        for (Iterator it=names.iterator(); it.hasNext();) {
            if (sb.length() > 0) sb.append('|');
            sb.append(params.get(it.next()));
		}
        return getHmac(sb.toString(), key.getBytes());
    }

    private static String getHmac(String data, byte[] key) {
        try {
            ByteKey bk = new ByteKey(key);
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(bk);
            return toHex(mac.doFinal(data.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("fatal error", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("fatal error", e);
        }
    }
    
    static class ByteKey implements SecretKey {
        private byte[] mKey;

        ByteKey(byte[] key) {
            mKey = (byte[]) key.clone();;
        }

        public byte[] getEncoded() {
            return mKey;
        }

        public String getAlgorithm() {
            return "HmacSHA1";
        }

        public String getFormat() {
            return "RAW";
        }
	}

   	public static String toHex(byte[] data) {
		StringBuilder sb = new StringBuilder(data.length * 2);
        for (int i=0; i<data.length; i++ ) {
           sb.append(hex[(data[i] & 0xf0) >>> 4]);
           sb.append(hex[data[i] & 0x0f] );
        }
        return sb.toString();
    }
   	
   	private static String generateDomainKey(String accountName){
   		String extracted = null;
		extracted = accountName.substring(accountName.indexOf("@") + 1);

		byte[] digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(extracted.getBytes("UTF-8")); // Change this to "UTF-16" if needed
			digest = md.digest();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new RuntimeException("fatal error", e);
		}

		return DatatypeConverter.printHexBinary(digest);	
	}

    private static final char[] hex =
       { '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' ,
         '8' , '9' , 'a' , 'b' , 'c' , 'd' , 'e' , 'f'};

%><%

String redirect = generateRedirect(request);
response.sendRedirect(redirect);	

%>
<html>
<head>
<title>Pre-auth redirect</title>
</head>
<body>

You should never see this page.

</body>
</html>