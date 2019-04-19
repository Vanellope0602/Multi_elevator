package multielevator;

import com.oocourse.TimableOutput;
import java.util.ArrayList;

public class Elevator implements Runnable {
    private final String id;
    private final int maximum;
    private final int runningTime;
    private final int openCloseTime = 200;
    private int floor = 1; // -3 ~ 20 floor
    private boolean running = false; // 判断是否在上下移动的状态
    private boolean open = false; // initialized
    private boolean closed = true; //
    private int direction = 1; // 方向：0代表下行，1代表上行
    private boolean stopSignal = false;

    private ArrayList<Person> personPool = new ArrayList<>(); // 注意每个电梯容量有限制
    private TaskQueue upQueue = new TaskQueue();
    private TaskQueue downQueue = new TaskQueue();
    //private TaskQueue waitingQueue = new TaskQueue();

    public Elevator(String s, int max, int time) {
        this.id = s;
        this.maximum = max;
        this.runningTime = time;
    }

    public synchronized void putInUp(Person person) {
        this.upQueue.putQueueTail(person);
        notifyAll();
        //System.out.println("put " + person.getId() + " into upQueue");
    } // 这里的notify必须要有，为什么不能同时插入好几个请求了？

    public synchronized void putInDown(Person person) {
        this.downQueue.putQueueTail(person);
        notifyAll();
        //System.out.println("put " + person.getId() + " into downQueue");
    }

    public synchronized Person getMain() {
        Person p = new Person();
        if (!this.hasPeople()) { // 如果电梯没有人
            while (this.upQueue.isEmpty() && this.downQueue.isEmpty()) {
                //System.out.println("All queue is empty! wait" + this.id);
                try {
                    wait();
                } catch (Exception e) {
                    System.out.println("ALL EMPTY ELEVATOR AND QUEUE");
                }
            } // 则从最早到队列中取出任务
            if (!this.upQueue.isEmpty()) { //先往上走
                p = this.upQueue.getQueueElm(0);
            } else if (!this.downQueue.isEmpty()) {
                p = this.downQueue.getQueueElm(0);
            }
        } else {
            //System.out.println("There are people in elevator, get number 0");
            p = this.getPerson(0);
        }
        //System.out.println("Now got a person request.");
        //notifyAll();
        return p;
    }

    @Override
    public void run() {
        // 先从队列里取用
        while (true) {
            //System.out.println("Next main request ");
            Person p = this.getMain(); // 获得下一个主请求
            //System.out.println("got main request: " + this.id + " run");
            if (p.isGoingUp()) {
                do {
                    if (this.onWhichFloor() != p.getSrcFloor()) {
                        try {
                            this.PickUp(this.onWhichFloor(), p.getSrcFloor());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } // 先接人
                    try { // 送上去
                        this.Crawl(p.getSrcFloor(),p.getDstFloor());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try { // 送电梯里剩余的乘客
                        this.carryLeftPeople();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } while (!this.upQueue.isEmpty()
                        && this.onWhichFloor() <= p.getSrcFloor()
                        && p.isGoingUp());

            } else { // p is going down
                do {
                    if (this.onWhichFloor() != p.getSrcFloor()) {
                        try {
                            this.PickUp(this.onWhichFloor(), p.getSrcFloor());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try { // 往下走
                        this.Slide(p.getSrcFloor(),p.getDstFloor());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try { // carry left people
                        this.carryLeftPeople();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } while (!this.downQueue.isEmpty()
                        && this.onWhichFloor() >= p.getSrcFloor()
                        && p.isGoingDown());

            }

        }
    }

    public synchronized boolean goIn(int floor,Person p) { // 走进来一个人
        if (this.peopleNum() < this.maximum) {
            this.personPool.add(p);
            TimableOutput.println(
                    String.format("IN-%d-%d-%s",p.getId(),floor,this.id));
            return true;
        } else {
            //System.out.println("now elevator has " +
            //peopleNum() + " people FULL\nwait for next ride");
            return false;
        }
        // 根据返回消息看看是否上电梯成功，要不要等待或者换一个电梯
    }

    public synchronized void goOut(int floor, Person p) { // 走出去一个人（到了目的地）
        this.personPool.remove(p);
        //System.out.println("now elevator has " + peopleNum() + " people");
        TimableOutput.println(
                String.format("OUT-%d-%d-%s",p.getId(),floor,this.id));
    }

    public void PickUp(int from, int to) throws InterruptedException {
        if (from > to) {
            this.Slide(from, to);
        } else if (from < to) {
            try {
                this.Crawl(from, to);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void Crawl(int from, int to) throws InterruptedException {
        for (int i = from; i <= to; i++) {
            if (i == 0) { continue; } //没有第0层
            this.onThisFloor(i);
            if (i > from) {
                Thread.sleep(this.runningTime);
                this.Arrive(i); // OUTPUT
            }
            //先检查有无人下电梯
            for (int k = 0; k < this.peopleNum() && this.hasPeople();) {
                Person p = this.getPerson(k);
                while (p.getDstFloor() == i) {
                    // 放人下来
                    this.Open(i);
                    this.goOut(i,p); // 走出电梯
                    p.ArriveDst();
                    if (p.isExchange()) { p.hasArrivedFirst(); }
                    if (!this.hasPeople()
                            || k >= this.peopleNum() - 1) {
                        if (k == 0 && this.hasPeople()) {
                            p = this.getPerson(0);
                            continue;
                        } else {
                            break;
                        }
                    }
                    p = this.getPerson(k);
                }
                k++;
            }
            // 检查有无人上电梯
            for (int j = 0; j < this.upQueue.size()
                    && !this.upQueue.isEmpty(); ) {
                //System.out.println("Upqueue size is " + this.upQueue.size());
                Person p = this.upQueue.getQueueElm(j);
                while (p.getSrcFloor() == i && p.isGoingUp()
                        && p.hasArrivedFirst()) {
                    if (this.peopleNum() == this.maximum) { break; }
                    // 有人上行且在该楼层出发，停下接人
                    this.Open(i);
                    this.goIn(i,p);
                    this.upQueue.kickOut(p); // 一旦进入电梯，则从队列中KickOut
                    if (this.upQueue.isEmpty()
                            || j >= this.upQueue.size() - 1) {
                        if (j == 0 && !this.upQueue.isEmpty()) {
                            p = this.upQueue.getQueueElm(0);
                            continue;
                        } else {
                            break;
                        }
                    }
                    p = this.upQueue.getQueueElm(j);
                }
                j++;
            }
            this.Close(i);
        }

    }

    public void Slide(int from, int to) throws InterruptedException {
        for (int i = from; i >= to; i--) {
            if (i == 0) { continue; }
            this.onThisFloor(i);
            if (i < from) {
                Thread.sleep(this.runningTime);
                this.Arrive(i);
            }
            for (int k = 0; k < this.peopleNum() && this.hasPeople();) {
                Person p = this.getPerson(k);
                while (p.getDstFloor() == i) {
                    // 放人下来
                    this.Open(i);
                    this.goOut(i, p);
                    p.ArriveDst();
                    if (p.isExchange()) { p.hasArrivedFirst(); }
                    if (!this.hasPeople()
                            || k >= this.peopleNum() - 1) {
                        if (k == 0 && this.hasPeople()) {
                            p = this.getPerson(0);
                            continue;
                        } else {
                            break;
                        }
                    }
                    p = this.getPerson(k);
                }
                k++;
            }

            for (int j = 0; j < this.downQueue.size()
                    && !this.downQueue.isEmpty();) {
                //System.out.println("Dqueue size " + this.downQueue.size());
                Person p = this.downQueue.getQueueElm(j);
                while (p.getSrcFloor() == i && p.isGoingDown()
                        && p.hasArrivedFirst()) {
                    if (this.peopleNum() == this.maximum) { break; }
                    this.Open(i);
                    this.goIn(i, p);
                    this.downQueue.kickOut(p);
                    //p.goIn(); // 设置人的状态
                    if (this.downQueue.isEmpty()
                            || j >= this.downQueue.size() - 1) {
                        if (j == 0 && !this.downQueue.isEmpty()) {
                            p = this.downQueue.getQueueElm(0);
                            continue;
                        } else {
                            break;
                        }
                    }
                    p = this.downQueue.getQueueElm(j);
                }
                j++;
            }
            this.Close(i);
        }
    }

    public void carryLeftPeople()
            throws InterruptedException {
        Person pt;
        while (this.hasPeople()) {
            pt = this.getPerson(0);
            if (this.onWhichFloor() > pt.getDstFloor()) {
                this.setGoDown();
                this.Slide(this.onWhichFloor(), pt.getDstFloor());
            } else {
                this.setGoUp();
                this.Crawl(this.onWhichFloor(), pt.getDstFloor());
            }
        }
    }

    public boolean hasPeople() { // 电梯里还有人
        return (!this.personPool.isEmpty());
    }

    public Person getPerson(int i) {
        return this.personPool.get(i);
    }

    public int peopleNum() {
        return this.personPool.size();
    }

    public void setGoUp() {
        this.direction = 1;
    }

    public void setGoDown() {
        this.direction = 0;
    }

    public boolean isGoUp() {
        return (this.direction == 1);
    }

    public boolean isGoDown() {
        return (this.direction == 0);
    }

    public int onWhichFloor() {
        return this.floor;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void Arrive(int floor) {
        TimableOutput.println(String.format("ARRIVE-%d-%s",floor,this.id));
    }

    public synchronized void Open(int floor) throws InterruptedException {
        //System.out.println("Open elevator!");
        if (this.isClosed()) {
            TimableOutput.println(String.format("OPEN-%d-%s",floor, this.id));
            Thread.sleep(openCloseTime);
        }
        this.open = true;
        this.closed = false;
    }

    public synchronized void Close(int floor) throws InterruptedException {
        //System.out.println("Close elevator!");
        if (this.isOpen()) {
            Thread.sleep(openCloseTime);
            TimableOutput.println(String.format("CLOSE-%d-%s",floor, this.id));
        }
        this.open = false;
        this.closed = true;
    }

    public boolean isOpen() {
        return this.open;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void onThisFloor(int floorIn) {
        this.floor = floorIn;
    }

    public void Run() {
        this.running = true;
    }

    public void Stop() {
        this.running = false;
    }

    public void setStopSignal() {
        this.stopSignal = true;
    }

    public boolean allEmpty() {
        if (this.downQueue.isEmpty() && this.upQueue.isEmpty()
                && !this.hasPeople() && this.personPool.isEmpty()
                && this.isClosed()) {
            return true;
        } else {
            return false;
        }
    }

}
