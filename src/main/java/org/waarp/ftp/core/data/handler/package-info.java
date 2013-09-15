/**
 * Classes implementing Data connections
 * 
 * <br>
 * <br>
 * The internal logic is the following:<br>
 * <ul>
 * <li>When a connection is opened for data network:</li>
 * It first tries to find the corresponding session setup from the control connection. Then it
 * setups the FTP codec. Finally it informs the FtpTransferControl object that the data connection
 * is opened and ready, such that the transfer can start correctly.
 * <li>Each time a block is received:</li>
 * the DataBlock is written into the corresponding FtpFile according to the status.
 * <li>If the operation is a retrieve</li> it writes the file to the data channel (from
 * FtpTransferControl) and wake up the process of writing from channelInterestChanged in order to
 * prevent OOME.
 * <li>When an exception occurs</li> the data connection will be closed so as the current transfer
 * action through the FtpTransferControl Object.
 * <li>When the connection is closed</li> the process tries to unbind if necessary the parent
 * connection (no more connections will use this binded address) and then cleans all attributes.
 * </ul>
 * 
 * @apiviz.landmark
 */
package org.waarp.ftp.core.data.handler;

