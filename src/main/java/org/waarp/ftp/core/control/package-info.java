/**
 * Classes implementing Control connections.
 * 
 * <br>
 * <br>
 * The internal logic is the following:<br>
 * <ul>
 * <li>When a connection is opened for control network:</li>
 * It first creates the default startup command (ConnectionCommand), then it answers it is ok to
 * accept identification (which is implied by ConnectionCommand).
 * <li>Each time a command is received:</li>
 * <ul>
 * <li>Parsing the command</li> in order to find the corresponding class that implements it.
 * <li>Checking if the command is legal now</li> such that no transfer is currently running except
 * if is a special command (like QUIT or ABORT).
 * <li>Checking if the command is legal from workflow</li> that is to say the previous command
 * allows the usage of the current command (for instance, no transfer command is allowed if the
 * authentication is not finished).
 * <li>Running the command</li> with executing a pre and post operation on business handler.
 * <li>Making the final answer of the command</li> (in some cases this is a partial answer like
 * ready to transfer)
 * </ul>
 * <li>When an exception occurs</li> the connection will be closed. <li>When the connection is
 * closed</li> all attributes are cleaned. </ul>
 * 
 * @apiviz.landmark
 */
package org.waarp.ftp.core.control;

