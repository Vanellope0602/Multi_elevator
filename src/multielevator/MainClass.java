package multielevator;

import com.oocourse.TimableOutput;

public class MainClass {
    public static void main(String[] args) throws Exception {
        TimableOutput.initStartTimestamp();
        Elevator e1 = new Elevator("A",6, 400); // 时间按毫秒计算
        Elevator e2 = new Elevator("B",8,500);
        Elevator e3 = new Elevator("C",7,600);
        Dispatch dispatch = new Dispatch(e1,e2,e3);
        InputHandler input = new InputHandler(dispatch);

        input.start();
        dispatch.start();
    }
}
