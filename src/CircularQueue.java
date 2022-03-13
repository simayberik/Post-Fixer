
public class CircularQueue {
	private int rear, front;
	private Object[] elements;
	public CircularQueue(int capacity) {
		this.elements = new Object[capacity];
		rear = -1;
		front = 0;
	}
	public void enqueue(Object data) {
		if (isFull()) {
			System.out.print("Queue overflow");
		}
		else {
			rear = (rear + 1) % elements.length;
			elements[rear] = data;
		}
	}
	public Object dequeue() {
		if (isEmpty()) {
			System.out.print("Circular Queue is full");
			return null;
		}
		else {
			Object data = elements[front];
			elements[front] =null;
			if (front == elements.length - 1 && front == rear) {
				rear = -1;
			}
			front = (front +1) % elements.length;
			return data;
		}
	}
	public Object peek() {
		if (isEmpty()) {
			System.out.print("Queue is empty");
			return null;
		}
		else {
			return elements[front];
		}
	}
	public boolean isFull() {
		return(front == (rear +1) % elements.length && elements[front] != null &&
				elements[rear] != null);
	}
	public boolean isEmpty() {
		return(elements[front] == null);
	}
	public int size() {
		if (rear >= front) {
			return rear - front + 1;
		}
		else if (elements[front] != null) {
			return elements.length - (front - rear) + 1;
		}
		else {
			return 0;
		}
	}
}
