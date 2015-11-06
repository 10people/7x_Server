import com.qx.http.Client;


public class ClientTest {
	public static void main (String... as){
		Client c = new Client("localhost", 8080);
		c.sendRequest("/qxrouter/junZhu.jsp", "Today is a wonderful day");
		System.out.print("finished!!!!");
	}

}
