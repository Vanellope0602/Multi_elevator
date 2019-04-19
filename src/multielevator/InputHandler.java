package multielevator;

import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.PersonRequest;
import java.io.IOException;

public class InputHandler extends Thread {
    private Dispatch dispatcher;

    public InputHandler(Dispatch dispatcher1) {
        this.dispatcher = dispatcher1;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);

        while (true) {
            PersonRequest request = elevatorInput.nextPersonRequest();
            // when request == null
            // it means there are no more lines in stdin
            if (request == null) {
                break;
            } else {
                Person p = new Person(request.getPersonId(),
                        request.getFromFloor(),request.getToFloor());
                try {
                    dispatcher.putInPerson(p);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //System.out.println("Ready for next request!");
            }
        }
        try {
            elevatorInput.close();
            dispatcher.canStop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dispatcher.canStop();
        while (!dispatcher.AllStop()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (dispatcher.AllStop()) {
            System.exit(0);
        }
    }

}
