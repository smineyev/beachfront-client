package mvw.client;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

public class ClientApplication {
	
	private static AtomicInteger responseCounter = new AtomicInteger(0);
	private static AtomicInteger timeoutCounter = new AtomicInteger(0);
	
	public static void run(String serverHost, int timeout, int qps) {
		AsyncHttpClient httpClient = Dsl.asyncHttpClient();
		BoundRequestBuilder getRequest = httpClient.prepareGet(
				"http://"+serverHost+":8080/getDisplay?userid=1&displayid=2&timeout="+timeout);
		
		long interval = 1000000*1000 / qps;
		long nextTime = System.nanoTime() + interval;
		while (true) {
			getRequest.execute(new AsyncCompletionHandler<Object>() {
			    @Override
			    public Object onCompleted(Response response) throws Exception {
			    	int rNum = responseCounter.incrementAndGet();
			    	int tNUm;
			    	if (response.getStatusCode() == 204) {
			    		tNUm = timeoutCounter.incrementAndGet();
			    	} else {
			    		tNUm = timeoutCounter.get();
			    	}
			    	if (rNum%1000 == 0) {
				    	System.out.println("#"+rNum+ ", timeouts: "+ tNUm);
				    	if (response.getStatusCode() == 200) {
				    		System.out.println(": "+response.getResponseBody());
				    	}
			    	}
			        return response;
			    }
			});
			
			while (System.nanoTime() < nextTime) {
				Thread.yield();
				// Thread.sleep(0, nextTime-ystem.nanoTime());
			}
			nextTime = nextTime + interval;
		}
		
	}

	public static void main(String[] args) throws Exception {
		String serverHost = args[0];
		int timeout = Integer.parseInt(args[1]);
		int qps = Integer.parseInt(args[2]);
		
		System.out.println("Server host: "+serverHost);
		System.out.println("Timeout: "+timeout);
		System.out.println("Target qps: "+qps);
		
		run(serverHost, timeout, qps);
		
//		int parallelism = ForkJoinPool.getCommonPoolParallelism();
//		ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);
//		for (int i=0; i<parallelism; i++) {
//			forkJoinPool.submit(() -> run(serverHost, timeout, qps/parallelism));
//		}
//		forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
	}

}
