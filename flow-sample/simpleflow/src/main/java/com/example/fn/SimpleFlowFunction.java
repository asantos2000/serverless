package com.example.fn;

import com.fnproject.fn.api.flow.Flow;
import com.fnproject.fn.api.flow.Flows;

public class SimpleFlowFunction {

    public String handleRequest(int x) {

	Flow fl = Flows.currentFlow();

	return fl.completedValue(x)
				.thenApply( i -> i * 2 )
				.thenApply( i -> "Your number is " + i )
				.get();	
    }
}