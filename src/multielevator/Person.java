package multielevator;

public class Person {
    private int id;
    private int srcFloor;
    private int dstFloor;
    private boolean inElevator; // to mark person IN elevator or OUT
    private int direction = -1;
    private int currentFloor;
    private boolean exchange = false;
    //private boolean canGoExchange = false;
    private Person ancestor;

    public Person() {

    }

    public Person(int id, int src, int dst) {
        this.id = id;
        this.srcFloor = src;
        this.dstFloor = dst;
        this.currentFloor = src;
        this.inElevator = false;
        if (dstFloor > srcFloor) {
            this.direction = 1; // 人要上行
        } else if (dstFloor < srcFloor) {
            this.direction = 0; // 人要下行
        }
        //System.out.println("This person's " + id
        //      + ": from " + src
        //    + " to " + dst);
    }

    public boolean isGoingUp() {
        return (this.direction == 1);
    }

    public boolean isGoingDown() {
        return (this.direction == 0);
    }

    public int getDirection() {
        return this.direction;
    }

    public boolean isInElevator() {
        return inElevator;
    }

    public int getId() {
        return this.id;
    }

    public int getSrcFloor() {
        return this.srcFloor;
    }

    public int getDstFloor() {
        return this.dstFloor;
    }

    public boolean isOnThisFloor(int floor) {
        return (this.currentFloor == floor);
    }

    public void goIn() { // 人走进电梯
        this.inElevator = true;
    }

    public void goOut() { // 人走出电梯
        this.inElevator = false;
    }

    public void ArriveDst() {
        this.currentFloor = this.dstFloor;
    }

    public void needExchange(Person pre) {
        this.ancestor = pre;
        this.exchange = true;
    }

    public boolean hasArrivedFirst() {
        if (this.exchange == true) {
            if (this.ancestor.isOnThisFloor(this.ancestor.getDstFloor())) {
                return true;
            } else {
                //System.out.println("Exchange has not arrived first yet!");
                return false;
            }
        }
        else { // 本来就不需要换乘，直接true就上去了！
            //System.out.println("No need for exchange person!");
            return true;
        }
    }

    public boolean isExchange() {
        return (this.exchange);
    }

}
