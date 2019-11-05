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
	
	public static void run(String serverHost, int qps) {
		System.out.println("Server host: "+serverHost);
		System.out.println("Target qps: "+qps);
		
		AsyncHttpClient httpClient = Dsl.asyncHttpClient();
		BoundRequestBuilder getRequest = httpClient.prepareGet(
				"http://"+serverHost+":8080/getDisplay?userid=1&displayid=2&timeout=3000");
		
		long interval = 1000000*1000 / qps;
		long nextTime = System.nanoTime() + interval;
		while (true) {
			getRequest.execute(new AsyncCompletionHandler<Object>() {
			    @Override
			    public Object onCompleted(Response response) throws Exception {
			    	int rNum = responseCounter.incrementAndGet();
//			    	if (rNum%1000 == 0) {
				    	System.out.print("#"+rNum+ ": "+ response.getStatusCode());
				    	if (response.getStatusCode() == 200) {
				    		System.out.println(": "+response.getResponseBody());
				    	} else {
				    		System.out.println();
				    	}
//			    	}
			        return response;
			    }
			});
			
			while (System.nanoTime() < nextTime) {
				Thread.yield();
			}
			nextTime = nextTime + interval;
		}
		
	}

	public static void main(String[] args) throws Exception {
		String serverHost = args[0];
		int qps = Integer.parseInt(args[1]);
		
		run(serverHost, qps);
//		int parallelism = ForkJoinPool.getCommonPoolParallelism();
//		ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);
//		for (int i=0; i<parallelism; i++) {
//			forkJoinPool.submit(() -> run(serverHost, qps/parallelism));
//		}
//		forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
	}

}
