package multielevator;

import java.util.ArrayList;

public class Dispatch extends Thread {
    private ArrayList<Person> mainQueue = new ArrayList<>();
    private Elevator elevator1;
    private Elevator elevator2;
    private Elevator elevator3;

    public Dispatch(Elevator e1,Elevator e2,Elevator e3) {
        elevator1 = e1;
        elevator2 = e2;
        elevator3 = e3;
    }

    public void putInPerson(Person p) throws InterruptedException {
        mainQueue.add(p);
        int src = p.getSrcFloor();
        int dst = p.getDstFloor();
        // 先考虑不用换乘的情况
        if (putStraight(p,src,dst)) {
            //System.out.println("Can go straight!");
        } else { // 需要换乘,使用nearExchange找最近的换乘点
            //System.out.println("Passenger " + p.getId() + " need exchange");
            if (reachA(src) && reachB(dst)) { // A->B,换乘点有-2,-1,1,15离哪里最近去哪换
                int newsrc = nearExchangeab(src);
                putExchangePerson(p, src, newsrc, dst, elevator1, elevator2);

            } else if (reachB(src) && reachA(dst)) { // B->A
                int newsrc = nearExchangeab(src);
                putExchangePerson(p, src, newsrc, dst, elevator2, elevator1);

            } else if (reachA(src) && reachC(dst)) { // A->C
                int newsrc = nearExchangeac(src);
                putExchangePerson(p, src, newsrc, dst, elevator1, elevator3);

            } else if (reachC(src) && reachA(dst)) { // C->A
                int newsrc = nearExchangeac(src);
                putExchangePerson(p, src, newsrc, dst, elevator3, elevator1);

            } else if (reachB(src) && reachC(dst)) { // B->C
                int newsrc = nearExchangebc(src);
                putExchangePerson(p, src, newsrc, dst, elevator2, elevator3);

            } else if (reachC(src) && reachB(dst)) { // C->B
                int newsrc = nearExchangebc(src);
                putExchangePerson(p, src, newsrc, dst, elevator3, elevator2);
            }
        }
    }

    private ArrayList<Integer> clist = new ArrayList<Integer>()
    {
        {
            add(1);
            add(3);
            add(5);
            add(7);
            add(9);
            add(11);
            add(13);
            add(15);
        }
    };

    public int straight(int src, int dst) {
        // A:1;  B:2,  C:3
        if (reachA(src) && reachA(dst)) {
            return 1;
        } else if (reachB(src) && reachB(dst)) {
            return 2;
        } else if (reachC(src) && reachC(dst)) {
            return 3;
        } else {
            //System.out.println("No straight elevator!");
            return 0; // 不可直达
        }
    }

    public boolean reachA(int floor) {
        if (floor <= 1 || floor >= 15) {
            //System.out.println("elevator A can reach");
            return true; // A电梯能到的楼层
        } else {
            return false;
        }
    }

    public boolean reachB(int floor) {
        if ((-2 <= floor && floor <= 2) || (4 <= floor && floor <= 15)) {
            //System.out.println("elevator B can reach");
            return true; // B 电梯能到的楼层
        } else {
            return false;
        }
    }

    public boolean reachC(int floor) {
        if (clist.contains(floor)) {
            //System.out.println("elevator C can reach");
            return true; // C 电梯能到的楼层
        } else {
            return false;
        }
    }

    public boolean putStraight(Person p,int src, int dst) {
        if (straight(src,dst) == 1) { // A 直达
            if (p.isGoingUp()) { elevator1.putInUp(p); }
            else { elevator1.putInDown(p); }
            return true;
        } else if (straight(src,dst) == 2) { // B直达
            if (p.isGoingUp()) { elevator2.putInUp(p); }
            else { elevator2.putInDown(p); }
            return true;
        } else if (straight(src,dst) == 3) { // C直达
            if (p.isGoingUp()) { elevator3.putInUp(p); }
            else { elevator3.putInDown(p); }
            return true;
        }
        else {
            return false;
        }
    }

    public int nearExchangeab(int tmp) { // 传进来的参数是p的出发楼层
        //-2,-1,1,15
        int[] arr = new int[4];
        arr[0] = Math.abs(-2 - tmp);
        arr[1] = Math.abs(-1 - tmp);
        arr[2] = Math.abs(1 - tmp);
        arr[3] = Math.abs(15 - tmp);
        int minIndex = arr[0];//定义最小值为该数组的第一个数
        for (int i = 0; i < arr.length; i++) {
            if (minIndex > arr[i]) {
                minIndex = arr[i];
            }
        }
        if (minIndex == arr[0]) { return -2; }
        else if (minIndex == arr[1]) { return -1; }
        else if (minIndex == arr[2]) { return 1; }
        else if (minIndex == arr[3]) { return 15; }
        else {
            //System.out.println("cannot find the nearest exchange floor!");
            return 0;
        }
    }

    public int nearExchangeac(int tmp) {
        //1,15
        if (Math.abs(1 - tmp) <= Math.abs(15 - tmp)) {
            return 1; // 要是一样进的话优先在1楼换乘吧！
        } else {
            return 15;
        }
    }

    public int nearExchangebc(int tmp) {
        //1,5,7,9,11,13,15
        int[] arr = new int[7];
        arr[0] = Math.abs(1 - tmp);
        arr[1] = Math.abs(5 - tmp);
        arr[2] = Math.abs(7 - tmp);
        arr[3] = Math.abs(9 - tmp);
        arr[4] = Math.abs(11 - tmp);
        arr[5] = Math.abs(13 - tmp);
        arr[6] = Math.abs(15 - tmp);

        int minIndex = arr[0];//定义最小值为该数组的第一个数
        for (int i = 0; i < arr.length; i++) {
            if (minIndex > arr[i]) {
                minIndex = arr[i];
            }
        }
        if (minIndex == arr[0]) { return 1; }
        else if (minIndex == arr[1]) { return 5; }
        else if (minIndex == arr[2]) { return 7; }
        else if (minIndex == arr[3]) { return 9; }
        else if (minIndex == arr[4]) { return 11; }
        else if (minIndex == arr[5]) { return 13; }
        else if (minIndex == arr[6]) { return 15; }
        else {
            //System.out.println("cannot find the nearest exchange floor!");
            return 0;
        }
    }

    public void putExchangePerson(Person p, int src,
            int newsrc, int dst, Elevator elevatorFirst, Elevator elevatorPick)
            throws InterruptedException {
        Person newp1 = new Person(p.getId(), src, newsrc);
        Person newp2 = new Person(p.getId(), newsrc, dst);
        newp2.needExchange(newp1);
        if (newp1.isGoingUp()) { elevatorFirst.putInUp(newp1); }
        else { elevatorFirst.putInDown(newp1); }
        /*while (!newp1.isOnThisFloor(newsrc)) {
            //System.out.println("not arrive yet!");
            Thread.sleep(100);
            //wait();
        }
        */
        if (newp2.isGoingUp()) { elevatorPick.putInUp(newp2); }
        else { elevatorPick.putInDown(newp2); }

    }

    public void work() {
        Thread t1 = new Thread(elevator1);
        Thread t2 = new Thread(elevator2);
        Thread t3 = new Thread(elevator3);
        t1.start();
        t2.start();
        t3.start();
    }

    @Override
    public void run() {
        //System.out.println("Into dispatch run");
        work();
    }

    public void canStop() {
        elevator1.setStopSignal();
        elevator2.setStopSignal();
        elevator3.setStopSignal();
    }

    public boolean AllStop() {
        if (elevator1.allEmpty() && elevator2.allEmpty()
                && elevator3.allEmpty()) {
            //System.out.println("All zero ! can terminate program ");
            return true;
        } else {
            //System.out.println("Ele " + elevator.peopleNum() +
            // " up " + upQueue.size() + " down " + downQueue.size());
            return false;
        }
    }
}
