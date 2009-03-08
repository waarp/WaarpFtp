/**
 * Frederic Bregier LGPL 1 févr. 09 
 * FilesystemBasedFtpFile.java goldengate.ftp.core.file.filesystem GoldenGateFtp
 * frederic
 */
package goldengate.ftp.filesystembased;

import goldengate.ftp.core.command.exception.FtpCommandAbstractException;
import goldengate.ftp.core.command.exception.Reply501Exception;
import goldengate.ftp.core.command.exception.Reply550Exception;
import goldengate.ftp.core.command.exception.Reply553Exception;
import goldengate.ftp.core.file.FtpDir;
import goldengate.ftp.core.file.FtpFile;
import goldengate.ftp.core.logging.FtpInternalLogger;
import goldengate.ftp.core.logging.FtpInternalLoggerFactory;
import goldengate.ftp.core.session.FtpSession;
import goldengate.ftp.filesystembased.config.FilesystemBasedFtpConfiguration;
import goldengate.ftp.filesystembased.digest.FilesystemBasedDigest;
import goldengate.ftp.filesystembased.specific.FilesystemBasedFtpCommonsIo;
import goldengate.ftp.filesystembased.specific.FilesystemBasedFtpDirJdkAbstract;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;


/**
 * Filesystem implementation of a Directory<br>
 * Note : this class depends on Apache commons Io.
 * @author frederic
 * goldengate.ftp.core.file.filesystem FilesystemBasedFtpDir
 * 
 */
public abstract class FilesystemBasedFtpDir extends FtpDir {
	/**
	 * Internal Logger
	 */
	private static final FtpInternalLogger logger =
        FtpInternalLoggerFactory.getLogger(FilesystemBasedFtpDir.class);
	/**
	 * Class that handles specifity of one Jdk or another
	 */
	private static FilesystemBasedFtpDirJdkAbstract filesystemBasedFtpDirJdk = null;
	/**
	 * Init the dependant object according to internals of JDK
	 * @param filesystemBasedFtpDirJdkChoice
	 */
	public static void initJdkDependent(FilesystemBasedFtpDirJdkAbstract filesystemBasedFtpDirJdkChoice) {
		filesystemBasedFtpDirJdk = filesystemBasedFtpDirJdkChoice;
	}
	/**
	 * Normalize Path to Internal unique representation
	 * @param path
	 * @return the normalized path
	 */
	public static String normalizePath(String path) {
		return path.replace('\\', SEPARATORCHAR);
	}
	/**
	 * @param session
	 */
	public FilesystemBasedFtpDir(FtpSession session) {
		super(session);
		this.optsMLSx.setOptsModify((byte)1);
		this.optsMLSx.setOptsPerm((byte)1);
		this.optsMLSx.setOptsSize((byte)1);
		this.optsMLSx.setOptsType((byte)1);
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#validatePath(java.lang.String)
	 */
	@Override
	public String validatePath(String path) throws FtpCommandAbstractException {
		String extDir = this.consolidatePath(path);
		FilesystemBasedFtpAuth ftpAuth = ((FilesystemBasedFtpAuth) this.getFtpSession().getFtpAuth());
		// Get the baseDir (mount point)
		String baseDir = ftpAuth.getAbsolutePath(null);
		// Get the translated real file path (removing '..')
		File newDir = new File(baseDir,extDir);
		logger.debug("Will validate {}",newDir);
		return validatePath(newDir);
	}
	/**
	 * Consolidate Path as relative or absolute path to an absolute path
	 * @param path
	 * @return the consolidated path
	 * @throws Reply501Exception 
	 */
	protected String consolidatePath(String path) throws Reply501Exception {
		if ((path == null) || (path.length() == 0)) {
			throw new Reply501Exception("Path must not be empty");
		}
		// First check if the path is relative or absolute
		String extDir = null;
		if (path.charAt(0) == SEPARATORCHAR) {
			extDir = path;
		} else {
			extDir = this.currentDir+SEPARATOR+path;
		}
		return extDir;
	}
	/**
	 * Same as validatePath but from a File
	 * @param dir
	 * @return the construct and validated path (could be different than the one given as argument, example: '..' are removed)
	 * @throws FtpCommandAbstractException
	 */
	protected String validatePath(File dir) throws FtpCommandAbstractException {
		FilesystemBasedFtpAuth ftpAuth = ((FilesystemBasedFtpAuth) this.getFtpSession().getFtpAuth());
		String extDir = null;
		try {
			extDir = normalizePath(dir.getCanonicalPath());
		} catch (IOException e) {
			throw new Reply550Exception("Internal error with Path name");
		}
		// Get the relative business path
		extDir = ftpAuth.getRelativePath(extDir);
		// Check if this business path is valid
		if (ftpAuth.isBusinessPathValid(extDir)) {
			logger.debug("ValidatePath: validated: {}",extDir);
			return extDir;
		}
		throw new Reply553Exception("Pathname not allowed");
	}
	/**
	 * Finds all files matching a wildcard expression (based on '?', '~' or '*').  
	 * @param pathWithWildcard The wildcard expression with a business path.
	 * @return List of String as relative paths matching the wildcard expression. 
	 * Those files are tested as valid from business point of view. 
	 * If Wildcard support is not active, if the path contains any wildcards, it will throw an error.
	 * @throws FtpCommandAbstractException 
	 */
	protected List<String> wildcardFiles(String pathWithWildcard) throws FtpCommandAbstractException
	{
		FilesystemBasedFtpAuth ftpAuth = (FilesystemBasedFtpAuth) this.getFtpSession().getFtpAuth();
		List<String> resultPaths = new ArrayList<String>();
		// First check if pathWithWildcard contains wildcards
		if (!(pathWithWildcard.contains("*") || pathWithWildcard.contains("?") || pathWithWildcard.contains("~"))) {
			// No so simply return the list containing this path after validating it
			if (ftpAuth.isBusinessPathValid(pathWithWildcard)) {
				logger.debug("Found simple {}",pathWithWildcard);
				resultPaths.add(pathWithWildcard);
			}
			return resultPaths;
		}
		// Do we support Wildcard path
		if (!FilesystemBasedFtpConfiguration.ueApacheCommonsIo) {
			throw new Reply553Exception("Wildcards in pathname is not allowed");
		}
		File rootFile = new File(ftpAuth.getAbsolutePath(null));
		File wildcardFile = new File(rootFile,pathWithWildcard);
		// Split wildcard path into subdirectories.
		List<String> subdirs = new ArrayList<String>();
		while (wildcardFile != null) {
			File parent = wildcardFile.getParentFile();
			logger.debug("Wildcard: current= {} parent= {}",wildcardFile,(parent != null ? parent : "no parent"));
			if (parent == null) {
				subdirs.add(0, wildcardFile.getPath());
				break;
			}
			subdirs.add(0, wildcardFile.getName());
			if (parent.equals(rootFile)) {
				// End of wildcard path
				subdirs.add(0,parent.getPath());
				break;
			}
			wildcardFile = parent;
		}
		List<File> basedPaths = new ArrayList<File>();
		// First set root
		basedPaths.add(new File(subdirs.get(0)));
		int i = 1;
		// For each wilcard subdirectory
		while(i < subdirs.size()) {
			// Set current filter
			logger.debug("Filter: {}",subdirs.get(i));
			FileFilter fileFilter = FilesystemBasedFtpCommonsIo.getWildcardFileFilter(subdirs.get(i));
			List<File> newBasedPaths = new ArrayList<File>();
			// Look for matches in all the current search paths
			for (File dir : basedPaths) {
				if (dir.isDirectory()) {
					for (File match : dir.listFiles(fileFilter)) {
						newBasedPaths.add(match);
					}
				}
			}
			// base Search Path changes now
			basedPaths = newBasedPaths;
			i++;
		}
		// Valid each file first
		for (File file : basedPaths) {
			String relativePath = ftpAuth.getRelativePath(normalizePath(file.getAbsolutePath()));
			String newpath = this.validatePath(relativePath);
			logger.debug("Wildcard found {}",newpath);
			resultPaths.add(newpath);
		}
		return resultPaths;
	}

	/**
	 * Get the File from this path, checking first its validity
	 * @param path
	 * @return the File
	 * @throws FtpCommandAbstractException 
	 */
	protected File getFileFromPath(String path) throws FtpCommandAbstractException {
		String newdir = validatePath(path);
		String truedir = ((FilesystemBasedFtpAuth) this.getFtpSession().getFtpAuth()).getAbsolutePath(newdir);
		logger.debug("getFile: {}",truedir);
		return new File(truedir);
	}
	/**
	 * Get the relative path (without mount point)
	 * @param file
	 * @return the relative path
	 */
	protected String getRelativePath(File file) {
		return ((FilesystemBasedFtpAuth) this.getFtpSession().getFtpAuth()).getRelativePath(file.getAbsolutePath());
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#getPwd()
	 */
	@Override
	public String getPwd() throws FtpCommandAbstractException {
		return this.currentDir;
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#changeDirectory(java.lang.String)
	 */
	@Override
	public boolean changeDirectory(String path) throws FtpCommandAbstractException {
		this.checkIdentify();
		String newpath = this.consolidatePath(path);
		List<String> paths = this.wildcardFiles(newpath);
		if (paths.size() != 1) {
			logger.warn("CD error: {}",newpath);
			throw new Reply550Exception("Directory not found: "+paths.size()+" founds");
		}
		String extDir = paths.get(0);
		extDir = this.validatePath(extDir);
		if (this.isDirectory(extDir)) {
			this.currentDir = extDir;
			return true;
		}
		throw new Reply550Exception("Directory not found");
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#changeParentDirectory()
	 */
	@Override
	public boolean changeParentDirectory()
			throws FtpCommandAbstractException {
		return changeDirectory("..");
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#mkdir(java.lang.String)
	 */
	@Override
	public String mkdir(String directory) throws FtpCommandAbstractException {
		this.checkIdentify();
		String newdirectory = this.consolidatePath(directory);
		File dir = new File(newdirectory);
		String parent = dir.getParentFile().getPath();
		List<String> paths = this.wildcardFiles(normalizePath(parent));
		if (paths.size() != 1) {
			throw new Reply550Exception("Base Directory not found: "+paths.size()+" founds");
		}
		String newDir = paths.get(0)+SEPARATOR+dir.getName();
		newDir = this.validatePath(newDir);
		File newdir = getFileFromPath(newDir);
		if (newdir.mkdir()) {
			return newDir;
		}
		throw new Reply550Exception("Cannot create directory "+newDir);
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#rmdir(java.lang.String)
	 */
	@Override
	public String rmdir(String directory) throws FtpCommandAbstractException {
		this.checkIdentify();
		String newdirectory = this.consolidatePath(directory);
		List<String> paths = this.wildcardFiles(normalizePath(newdirectory));
		if (paths.size() != 1) {
			throw new Reply550Exception("Directory not found: "+paths.size()+" founds");
		}
		String extDir = paths.get(0);
		extDir = this.validatePath(extDir);
		File dir = getFileFromPath(extDir);
		if (dir.delete()) {
			return extDir;
		}
		throw new Reply550Exception("Cannot delete directory "+extDir);
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#isDirectory(java.lang.String)
	 */
	@Override
	public boolean isDirectory(String path) throws FtpCommandAbstractException {
		this.checkIdentify();
		File dir = getFileFromPath(path);
		logger.debug("ISDIR: {} {}",dir,dir.isDirectory());
		return dir.isDirectory();
	}

	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#isFile(java.lang.String)
	 */
	@Override
	public boolean isFile(String path) throws FtpCommandAbstractException {
		this.checkIdentify();
		return getFileFromPath(path).isFile();
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpDir#getModificationTime(java.lang.String)
	 */
	@Override
	public String getModificationTime(String path) throws FtpCommandAbstractException {
		this.checkIdentify();
		File file = getFileFromPath(path);
		if (file.exists()) {
			return getModificationTime(file);
		}
		throw new Reply550Exception("\""+path+"\" does not exist");
	}
	/**
	 * Return the Modification time for the File
	 * @param file
	 * @return the Modification time as a String YYYYMMDDHHMMSS.sss
	 */
	protected String getModificationTime(File file) {
		long mstime = file.lastModified();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mstime);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		int ms = calendar.get(Calendar.MILLISECOND);
		StringBuilder sb = new StringBuilder(18);
		sb.append(year);
		if (month < 10) {
			sb.append(0);
		}
		sb.append(month);
		if (day < 10) {
			sb.append(0);
		}
		sb.append(day);
		if (hour < 10) {
			sb.append(0);
		}
		sb.append(hour);
		if (minute < 10) {
			sb.append(0);
		}
		sb.append(minute);
		if (second < 10) {
			sb.append(0);
		}
		sb.append(second);
		sb.append('.');
		if (ms < 10) {
			sb.append(0);
		}
		if (ms < 100) {
			sb.append(0);
		}
		sb.append(ms);
		return sb.toString();
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#list(java.lang.String)
	 */
	@Override
	public List<String> list(String path) throws FtpCommandAbstractException {
		this.checkIdentify();
		// First get all base directories
		String newpath = path;
		if (newpath.startsWith("-a") || newpath.startsWith("-A")) {
			String []args = newpath.split(" ");
			if (args.length > 1) {
				newpath = args[1];
			} else {
				newpath = this.currentDir;
			}
		}
		newpath = this.consolidatePath(newpath);
		logger.debug("List {}",newpath);
		List<String> paths = this.wildcardFiles(newpath);
		if (paths.isEmpty()) {
			throw new Reply550Exception("No files found");
		}
		// Now if they are directories, list inside them
		FilesystemBasedFtpAuth ftpAuth = (FilesystemBasedFtpAuth) this.getFtpSession().getFtpAuth();
		List<String> newPaths = new ArrayList<String>();
		for (String file : paths) {
			File dir = getFileFromPath(file);
			if (dir.exists()) {
				if (dir.isDirectory()) {
					String [] files = dir.list();
					for (String finalFile : files) {
						String relativePath = ftpAuth.getRelativePath(finalFile);
						newPaths.add(relativePath);
					}
				} else {
					newPaths.add(file);
				}
			}
		}
		return newPaths;
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#listFull(java.lang.String, java.lang.boolean)
	 */
	@Override
	public List<String> listFull(String path, boolean lsFormat) throws FtpCommandAbstractException {
		this.checkIdentify();
		boolean listAllFiles = false;
		String newpath = path;
		if (newpath.startsWith("-a") || newpath.startsWith("-A")) {
			String []args = newpath.split(" ");
			if (args.length > 1) {
				newpath = args[1];
			} else {
				newpath = this.currentDir;
			}
			listAllFiles = true;
		}
		newpath = this.consolidatePath(newpath);
		logger.debug("ListFull {}",newpath);
		// First get all base directories
		List<String> paths = this.wildcardFiles(newpath);
		if (paths.isEmpty()) {
			throw new Reply550Exception("No files found");
		}
		// Now if they are directories, list inside them
		List<String> newPaths = new ArrayList<String>();
		for (String file : paths) {
			File dir = getFileFromPath(file);
			if (dir.exists()) {
				if (dir.isDirectory()) {
					File [] files = dir.listFiles();
					for (File finalFile : files) {
						if (lsFormat) {
							newPaths.add(this.lsInfo(finalFile));
						} else {
							newPaths.add(this.mlsxInfo(finalFile));
						}
					}
				} else {
					if (lsFormat) {
						newPaths.add(this.lsInfo(dir));
					} else {
						newPaths.add(this.mlsxInfo(dir));
					}
				}
			}
		}
		if (listAllFiles) {
			File dir = new File(getFileFromPath(newpath),SEPARATOR+"..");
			if (lsFormat) {
				newPaths.add(this.lsInfo(dir));
			} else {
				newPaths.add(this.mlsxInfo(dir));
			}
		}
		return newPaths;
	}
	
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpDir#fileFull(java.lang.String, java.lang.boolean)
	 */
	@Override
	public String fileFull(String path, boolean lsFormat) throws FtpCommandAbstractException {
		this.checkIdentify();
		String newpath = this.consolidatePath(path);
		logger.debug("FileFull {}",newpath);
		List<String> paths = this.wildcardFiles(normalizePath(newpath));
		if (paths.size() != 1) {
			throw new Reply550Exception("No files found "+paths.size()+" founds");
		}
		File file = this.getFileFromPath(paths.get(0));
		if (file.exists()) {
			if (lsFormat) {
				return "Listing of \""+paths.get(0)+"\"\n"+lsInfo(file)+"\nEnd of listing";
			}
			return "Listing of \""+paths.get(0)+"\"\n"+mlsxInfo(file)+"\nEnd of listing";
		}
		return "No file with name \""+path+"\"";
	}
	/**
	 * Decide if Full time or partial time as in 'ls' command
	 * @return True if Full Time, False is Default (as in 'ls' command)
	 */
	protected boolean isFullTime() {
		//FIXME should be it the default ?
		return false;
	}

	/**
	 * 
	 * @param file
	 * @return the ls format information
	 */
	protected String lsInfo(File file) {
		// Unix File type,permissions,hard link(?),owner(?),group(?),size,date and filename
		StringBuilder builder = new StringBuilder();
		builder.append((file.isDirectory()?'d':'-'));
		builder.append((file.canRead()?'r':'-'));
		builder.append((file.canWrite()?'w':'-'));
		builder.append(filesystemBasedFtpDirJdk.canExecute(file)?'x':'-');
		// Group and others not supported
		builder.append("---");
		builder.append("---");
		builder.append(' ');
		builder.append("1 ");// hard link ?
		builder.append("anybody\t");//owner ?
		builder.append("anygroup\t");//group ?
		builder.append(file.length());//size
		builder.append('\t');
		long lastmod = file.lastModified();
		String fmt = null;
		// It seems Full Time is not recognized by some FTP client
		/*if(isFullTime()) {
			fmt = "EEE MMM dd HH:mm:ss yyyy";
		} else {*/
			long currentTime = System.currentTimeMillis();
			if( currentTime > lastmod + 6L * 30L * 24L * 60L * 60L * 1000L  // Old.
				|| currentTime < lastmod - 60L * 60L * 1000L ) { // In the future.
				//  The file is fairly old or in the future.
				//	POSIX says the cutoff is 6 months old;
				//	approximate this by 6*30 days.
				//	Allow a 1 hour slop factor for what is considered "the future",
				//	to allow for NFS server/client clock disagreement.
				//	Show the year instead of the time of day.
				fmt = "MMM dd  yyyy";
			} else {
				fmt = "MMM dd HH:mm";
			}
		/*}*/
		SimpleDateFormat dateFormat = 
			(SimpleDateFormat)DateFormat.getDateTimeInstance(DateFormat.LONG, 
				DateFormat.LONG, Locale.ENGLISH);
		dateFormat.applyPattern(fmt);
		builder.append(dateFormat.format(new Date(lastmod)));//date
		builder.append('\t');
		builder.append(file.getName());
		return builder.toString();
	}
	/**
	 * 
	 * @param file
	 * @return the MLSx information: ' Fact=facts;...; filename'
	 */
	protected String mlsxInfo(File file) {
		logger.debug("fullInfo of {}",file);
		// don't have create, unique, lang, media-type, charset
		StringBuilder builder = new StringBuilder();
		if (this.optsMLSx.getOptsSize() == 1) {
			builder.append(" Size=");
			builder.append(file.length());
			builder.append(';');
		}
		if (this.optsMLSx.getOptsModify() == 1) {
			builder.append("Modify=");
			builder.append(this.getModificationTime(file));
			builder.append(';');
		}
		if (this.optsMLSx.getOptsType() == 1) {
			builder.append("Type=");
			try {
				if (this.getFileFromPath(this.getPwd()).equals(file)) {
					builder.append("cdir");
				} else {
					if (file.isDirectory()) {
						builder.append("dir");
					} else {
						builder.append("file");
					}
				}
			} catch (FtpCommandAbstractException e) {
				if (file.isDirectory()) {
					builder.append("dir");
				} else {
					builder.append("file");
				}
			}
			builder.append(';');
		}
		if (this.optsMLSx.getOptsPerm() == 1) {
			builder.append("Perm=");
			if (file.isFile()) {
				if (file.canWrite()) {
					builder.append('a');
					builder.append('d');
					builder.append('f');
					builder.append('w');
				}
				if (file.canRead()) {
					builder.append('r');
				}
			} else {
				// Directory
				if (file.canWrite()) {
					builder.append('c');
					try {
						if (this.validatePath(file) != null) {
							builder.append('d');
							builder.append('m');
							builder.append('p');
						}
					} catch (FtpCommandAbstractException e) {
					}
				}
				if (file.canRead()) {
					builder.append('l');
					builder.append('e');
				}
			}
			builder.append(';');
		}
		
		builder.append(' ');
		builder.append(file.getName());
		return builder.toString();
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#getFreeSpace()
	 */
	@Override
	public long getFreeSpace() throws FtpCommandAbstractException {
		this.checkIdentify();
		File directory = getFileFromPath(this.currentDir);
		return filesystemBasedFtpDirJdk.getFreeSpace(directory);
	}

	
	
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#setFile(java.lang.String, boolean)
	 */
	@Override
	public FtpFile setFile(String path, boolean append)
			throws FtpCommandAbstractException {
		this.checkIdentify();
		String newpath = this.consolidatePath(path);
		List<String> paths = this.wildcardFiles(newpath);
		if (paths.size() != 1) {
			throw new Reply550Exception("File not found: "+paths.size()+" founds");
		}
		String extDir = paths.get(0);
		return this.newFtpFile(extDir, append);
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#setUniqueFile()
	 */
	@Override
	public FtpFile setUniqueFile() throws FtpCommandAbstractException {
		this.checkIdentify();
		File file = null;
		try {
			file = File.createTempFile(this.getFtpSession().getFtpAuth().getUser(), ".stou", this.getFileFromPath(this.currentDir));
		} catch (IOException e) {
			throw new Reply550Exception("Cannot create unique file");
		}
		String currentFile = this.getRelativePath(file);
		return this.newFtpFile(normalizePath(currentFile), false);
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#canRead()
	 */
	@Override
	public boolean canRead() throws FtpCommandAbstractException {
		this.checkIdentify();
		return getFileFromPath(this.currentDir).canRead();
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#canWrite()
	 */
	@Override
	public boolean canWrite() throws FtpCommandAbstractException {
		this.checkIdentify();
		File file = getFileFromPath(this.currentDir);
		return file.canWrite();
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#exists()
	 */
	@Override
	public boolean exists() throws FtpCommandAbstractException {
		this.checkIdentify();
		return getFileFromPath(this.currentDir).exists();
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#getCRC(String)
	 */
	@Override
	public long getCRC(String path) throws FtpCommandAbstractException {
		this.checkIdentify();
		String newpath = this.consolidatePath(path);
		List<String> paths = this.wildcardFiles(normalizePath(newpath));
		if (paths.size() != 1) {
			throw new Reply550Exception("File not found: "+paths.size()+" founds");
		}
		String extDir = paths.get(0);
		extDir = this.validatePath(extDir);
		File file = getFileFromPath(extDir);
		if (! file.isFile()) {
			throw new Reply550Exception("Path is not a file: "+path);
		}
		try {
            CheckedInputStream cis = null;
            try {
                // Computer CRC32 checksum
                cis = new CheckedInputStream(
                        new FileInputStream(file), 
                        new CRC32());
            } catch (FileNotFoundException e) {
                throw new Reply550Exception("File not found: "+path);
            }
            byte[] buf = new byte[this.getFtpSession().getConfiguration().BLOCKSIZE];
            while(cis.read(buf) >= 0) {
            }
            return cis.getChecksum().getValue();
        } catch (IOException e) {
            throw new Reply550Exception("Error while reading file: "+path);
        }
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#getMD5(String)
	 */
	@Override
	public byte[] getMD5(String path) throws FtpCommandAbstractException {
		this.checkIdentify();
		String newpath = this.consolidatePath(path);
		List<String> paths = this.wildcardFiles(normalizePath(newpath));
		if (paths.size() != 1) {
			throw new Reply550Exception("File not found: "+paths.size()+" founds");
		}
		String extDir = paths.get(0);
		extDir = this.validatePath(extDir);
		File file = getFileFromPath(extDir);
		if (! file.isFile()) {
			throw new Reply550Exception("Path is not a file: "+path);
		}
		try {
			if (FilesystemBasedFtpConfiguration.useNio) {
				return FilesystemBasedDigest.getHashMd5Nio(file);
			}
			return FilesystemBasedDigest.getHashMd5(file);
		} catch (IOException e1) {
			throw new Reply550Exception("Error while reading file: "+path);
		}
	}
	/* (non-Javadoc)
	 * @see goldengate.ftp.core.file.FtpFile#getSHA1(String)
	 */
	@Override
	public byte[] getSHA1(String path) throws FtpCommandAbstractException {
		this.checkIdentify();
		String newpath = this.consolidatePath(path);
		List<String> paths = this.wildcardFiles(normalizePath(newpath));
		if (paths.size() != 1) {
			throw new Reply550Exception("File not found: "+paths.size()+" founds");
		}
		String extDir = paths.get(0);
		extDir = this.validatePath(extDir);
		File file = getFileFromPath(extDir);
		if (! file.isFile()) {
			throw new Reply550Exception("Path is not a file: "+path);
		}
		try {
			if (FilesystemBasedFtpConfiguration.useNio) {
				return FilesystemBasedDigest.getHashSha1Nio(file);
			}
			return FilesystemBasedDigest.getHashSha1(file);
		} catch (IOException e1) {
			throw new Reply550Exception("Error while reading file: "+path);
		}
	}
}
