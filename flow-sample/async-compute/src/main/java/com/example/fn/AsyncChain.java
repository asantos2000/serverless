package com.example.fn;

import com.fnproject.fn.api.flow.Flow;
import com.fnproject.fn.api.flow.Flows;
import com.fnproject.fn.api.flow.FlowFuture;

public class AsyncChain {

    public long handleRequest() {

	  Flow fl = Flows.currentFlow();
	  
	  FlowFuture<Long> f1 = fl.supply(() -> {
		 long oneHour = 60 * 60 * 1000;
		 return System.currentTimeMillis() + oneHour;
	  });
	  
	  FlowFuture<Long> f2 = f1.thenApply( millis -> {
		long seconds = millis / 1000;
		return seconds;
	  });

      return f2.get();
    }
}