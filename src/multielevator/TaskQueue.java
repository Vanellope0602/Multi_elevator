package multielevator;

import java.util.ArrayList;

public class TaskQueue {
    private ArrayList<Person> queue = new ArrayList<>(1000);

    public void putQueueTail(Person a) { //放在队列的最后面
        queue.add(a);
    }

    public void putQueueHead(Person a) {
        queue.add(0,a);
    }

    public Person getQueueElm(int i) {
        return queue.get(i); // 返回的是一个需求
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean isFull() {
        return (queue.size() >= 1000);
    }

    public int size() {
        return queue.size();
    }

    public boolean kickOut(Person p) { // 把这个人移除队列（他已经进到电梯里了）
        return queue.remove(p);
    }

}
