package sr.grpc.example.server;

import sr.grpc.gen.ArithmeticOpResult;
import sr.grpc.gen.CalculatorGrpc.CalculatorImplBase;

public class CalculatorImpl extends CalculatorImplBase 
{
	@Override
	public void add(sr.grpc.gen.ArithmeticOpArguments request,
			io.grpc.stub.StreamObserver<ArithmeticOpResult> responseObserver)
	{
		System.out.println("addRequest (" + request.getArg1() + ", " + request.getArg2() +")");
		int val = request.getArg1() + request.getArg2();
		ArithmeticOpResult result = ArithmeticOpResult.newBuilder().setRes(val).build();
		if(request.getArg1() > 100 && request.getArg2() > 100) try { Thread.sleep(5000); } catch(InterruptedException ex) { }
		responseObserver.onNext(result);
		responseObserver.onCompleted();
	}

	@Override
	public void subtract(sr.grpc.gen.ArithmeticOpArguments request,
			io.grpc.stub.StreamObserver<ArithmeticOpResult> responseObserver)
	{
		System.out.println("subtractRequest (" + request.getArg1() + ", " + request.getArg2() +")");
		int val = request.getArg1() - request.getArg2();
		ArithmeticOpResult result = ArithmeticOpResult.newBuilder().setRes(val).build();
		responseObserver.onNext(result);
		responseObserver.onCompleted();
	}

	@Override
	public void multiply(sr.grpc.gen.MultiplicationArguments request,
						 io.grpc.stub.StreamObserver<ArithmeticOpResult> responseObserver) {

		if (request.getArgsCount() == 0) {
			responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
					.withDescription("Lista argumentów nie może być pusta")
					.asRuntimeException());
			return;
		}

		System.out.println("multiplyRequest (count: " + request.getArgsCount() + ")");
		long product = 1;
		for (int arg : request.getArgsList()) {
			product *= arg;
		}

		// Uwaga: Wynik rzutujemy na int32 zgodnie z definicją ArithmeticOpResult
		ArithmeticOpResult result = ArithmeticOpResult.newBuilder()
				.setRes((int) product)
				.build();

		responseObserver.onNext(result);
		responseObserver.onCompleted();
	}

}
