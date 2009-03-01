/**
 * 
 */
package goldengate.ftp.simpleimpl.config;

/**
 * Circular Value used by passive connections to find the next valid port
 * to propose to the client.
 * @author fbregier
 *
 */
public class CircularIntValue {
	private int min;
	private int max;
	private int current;
	/**
	 * Create a circular range of values
	 * @param min
	 * @param max
	 */
	public CircularIntValue(int min, int max) {
		this.min = min;
		this.max = max;
		this.current = this.min;
	}
	/**
	 * Get the next value
	 * @return the next value
	 */
	public synchronized int getNext() {
		int next = this.current;
		this.current++;
		if (this.current > this.max) {
			this.current = this.min;
		}
		return next;
	}
}
