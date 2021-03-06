package play.classloading;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.enhancers.ControllersEnhancer;
import play.classloading.enhancers.Enhancer;
import play.classloading.enhancers.JPAEnhancer;
import play.classloading.enhancers.LocalvariablesNamesEnhancer;
import play.classloading.enhancers.MailerEnhancer;
import play.classloading.enhancers.PropertiesEnhancer;
import play.classloading.enhancers.SigEnhancer;
import play.exceptions.UnexpectedException;
import play.vfs.VirtualFile;
import bran.StaticActionInvoker;
import bran.plugins.StaticRouterPlugin;

/**
 * Application classes container.
 */
public class ApplicationClasses {
	/**
	 * Reference to the eclipse compiler.
	 */
	public ApplicationCompiler compiler = new ApplicationCompiler(this);
	/**
	 * Cache of all compiled classes
	 */
	public Map<String, ApplicationClass> classes = new HashMap<String, ApplicationClass>();

	/**
	 * Clear the classes cache
	 */
	public void clear() {
		classes = new HashMap<String, ApplicationClass>();
	}

	/**
	 * Get a class by name
	 * 
	 * @param name
	 *            The fully qualified class name
	 * @return The ApplicationClass or null
	 */
	public ApplicationClass getApplicationClass(String name) {
		if (!classes.containsKey(name) && getJava(name) != null) {
			classes.put(name, new ApplicationClass(name));
		}
		return classes.get(name);
	}

	/**
	 * Retrieve all application classes assignable to this class.
	 * 
	 * @param clazz
	 *            The superclass, or the interface.
	 * @return A list of application classes.
	 */
	public List<ApplicationClass> getAssignableClasses(Class<?> clazz) {
		List<ApplicationClass> results = new ArrayList<ApplicationClass>();
		if (clazz != null) {
			for (ApplicationClass applicationClass : classes.values()) {
				if (!applicationClass.isClass())
					continue;
				try {
					Play.classloader.loadClass(applicationClass.name);
				} catch (ClassNotFoundException ex) {
					throw new UnexpectedException(ex);
				}
				try {
					if (clazz.isAssignableFrom(applicationClass.javaClass) && !applicationClass.javaClass.getName().equals(clazz.getName())) {
						results.add(applicationClass);
					}
				} catch (Exception e) {
				}
			}
		}
		return results;
	}

	/**
	 * Retrieve all application classes with a specific annotation.
	 * 
	 * @param clazz
	 *            The annotation class.
	 * @return A list of application classes.
	 */
	public List<ApplicationClass> getAnnotatedClasses(Class<? extends Annotation> clazz) {
		List<ApplicationClass> results = new ArrayList<ApplicationClass>();
		for (ApplicationClass applicationClass : classes.values()) {
			if (!applicationClass.isClass())
				continue;
			try {
				Play.classloader.loadClass(applicationClass.name);
			} catch (ClassNotFoundException ex) {
				throw new UnexpectedException(ex);
			}
			if (applicationClass.javaClass.isAnnotationPresent(clazz)) {
				results.add(applicationClass);
			}
		}
		return results;
	}

	/**
	 * All loaded classes.
	 * 
	 * @return All loaded classes
	 */
	public List<ApplicationClass> all() {
		return new ArrayList<ApplicationClass>(classes.values());
	}

	/**
	 * Put a new class to the cache.
	 */
	public void add(ApplicationClass applicationClass) {
		classes.put(applicationClass.name, applicationClass);
	}

	/**
	 * Remove a class from cache
	 */
	public void remove(ApplicationClass applicationClass) {
		classes.remove(applicationClass.name);
	}

	public void remove(String applicationClass) {
		classes.remove(applicationClass);
	}

	/**
	 * Does this class is already loaded ?
	 * 
	 * @param name
	 *            The fully qualified class name
	 */
	public boolean hasClass(String name) {
		return classes.containsKey(name);
	}

	// Enhancers
	static Class[] enhancers = new Class[] { SigEnhancer.class, ControllersEnhancer.class, MailerEnhancer.class, PropertiesEnhancer.class,
			JPAEnhancer.class, LocalvariablesNamesEnhancer.class };

	/**
	 * Represent a application class
	 */
	public static class ApplicationClass {

		/**
		 * The fully qualified class name
		 */
		public String name;
		/**
		 * A reference to the java source file
		 */
		public VirtualFile javaFile;
		/**
		 * The Java source
		 */
		public String javaSource;
		/**
		 * The compiled byteCode
		 */
		public byte[] javaByteCode;
		/**
		 * The enhanced byteCode
		 */
		public byte[] enhancedByteCode;
		/**
		 * The in JVM loaded class
		 */
		public Class<?> javaClass;
		/**
		 * The in JVM loaded package
		 */
		public Package javaPackage;
		/**
		 * Last time than this class was compiled
		 */
		public Long timestamp = 0L;
		/**
		 * Is this class compiled
		 */
		public boolean compiled;
		/**
		 * Signatures checksum
		 */
		public int sigChecksum;

		public ApplicationClass() {
		}

		public ApplicationClass(String name) {
			this.name = name;
			this.javaFile = getJava(name);
			this.refresh();
		}

		/**
		 * Need to refresh this class !
		 */
		public void refresh() {
			if (this.javaFile != null) {
				this.javaSource = this.javaFile.contentAsString();
			}
			this.javaByteCode = null;
			this.enhancedByteCode = null;
			this.compiled = false;
			this.timestamp = 0L;
		}

		/**
		 * Enhance this class
		 * 
		 * @return the enhanced byteCode
		 */
		public byte[] enhance() {
			this.enhancedByteCode = this.javaByteCode;
			// bran exclude the StaticActionInvoker
			if (isClass()) {
				for (Class<?> enhancer : enhancers) {
					try {
						long start = System.currentTimeMillis();
						((Enhancer) enhancer.newInstance()).enhanceThisClass(this);
						Logger.trace("%sms to apply %s to %s", System.currentTimeMillis() - start, enhancer.getSimpleName(), name);
					} catch (Exception e) {
						throw new UnexpectedException("While applying " + enhancer + " on " + name, e);
					}
				}
				for (PlayPlugin plugin : Play.plugins) {
					try {
						long start = System.currentTimeMillis();
						plugin.enhance(this);
						Logger.trace("%sms to apply %s to %s", System.currentTimeMillis() - start, plugin, name);
					} catch (Exception e) {
						throw new UnexpectedException("While applying " + plugin + " on " + name, e);
					}
				}
			}
			if (System.getProperty("precompile") != null) {
				try {
					// emit bytecode to standard class layout as well
					File f = Play.getFile("precompiled/java/" + (name.replace(".", "/")) + ".class");
					f.getParentFile().mkdirs();
					FileOutputStream fos = new FileOutputStream(f);
					fos.write(this.enhancedByteCode);
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return this.enhancedByteCode;

		}

		/**
		 * Is this class already compiled but not defined ?
		 * 
		 * @return if the class is compiled but not defined
		 */
		public boolean isDefinable() {
			return compiled && javaClass != null;
		}

		public boolean isClass() {
			return !name.endsWith("package-info");
		}

		public String getPackage() {
			int dot = name.lastIndexOf('.');
			return dot > -1 ? name.substring(0, dot) : "";
		}

		/**
		 * Compile the class from Java source
		 * 
		 * @return the bytes that comprise the class file
		 */
		public byte[] compile() {
			long start = System.currentTimeMillis();
			Play.classes.compiler.compile(new String[] { this.name });
			Logger.trace("%sms to compile class %s", System.currentTimeMillis() - start, name);
			return this.javaByteCode;
		}

		/**
		 * Unload the class
		 */
		public void uncompile() {
			this.javaClass = null;
		}

		/**
		 * Call back when a class is compiled.
		 * 
		 * @param code
		 *            The bytecode.
		 */
		public void compiled(byte[] code) {
			javaByteCode = code;
			enhancedByteCode = code;
			compiled = true;
			this.timestamp = this.javaFile.lastModified();
		}

		@Override
		public String toString() {
			return name + " (compiled:" + compiled + ")";
		}
	}

	// ~~ Utils
	/**
	 * Retrieve the corresponding source file for a given class name. It handles
	 * innerClass too !
	 * 
	 * @param name
	 *            The fully qualified class name
	 * @return The virtualFile if found
	 */
	public static VirtualFile getJava(String name) {
		String fileName = name;
		if (fileName.contains("$")) {
			fileName = fileName.substring(0, fileName.indexOf("$"));
		}
		fileName = fileName.replace(".", "/") + ".java";
		for (VirtualFile path : Play.javaPath) {
			VirtualFile javaFile = path.child(fileName);
			if (javaFile.exists()) {
				return javaFile;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return classes.toString();
	}

	// bran static routing
	public StaticActionInvoker getStaticActionInvoker() {
		if (staticActionInvoker == null) {
			findSAI1();
		}
		return staticActionInvoker;
	}

	private void findSAI2() {
		Play.classloader.getAllClasses();
		ApplicationClass c = getApplicationClass(StaticRouterPlugin.DISPATCHER_CLASS);
		if (c == null)
			return;
		Class<?> clz = null;

		if (/*Play.mode == Mode.PROD && */c.javaClass != null) {
			clz = c.javaClass;
		} else {
			clz = Play.classloader.loadApplicationClass(c.name);
		}
		Constructor<?> constructor;
		try {
			constructor = clz.getConstructor();
			StaticActionInvoker inst = (StaticActionInvoker) constructor.newInstance();
			staticActionInvoker = inst;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void findSAI1() {
		Play.classloader.getAllClasses();
		ApplicationClass ac = getApplicationClass(StaticRouterPlugin.DISPATCHER_CLASS);

		if (ac == null) {
			if (getJava(StaticRouterPlugin.DISPATCHER_JAVA) != null) {
				// ac = new ApplicationClass(ROUTER_DISPATCHER);
			} else
				return;
		}
		Class<?> clz = null;
		// if (Play.usePrecompiled) {
		if (ac.javaClass != null /* && ac.compiled */) {
			clz = ac.javaClass;
		} else {
			try {
				clz = Play.classloader.loadApplicationClass(ac.name);
			} catch (LinkageError e) {
				StaticRouterPlugin.rebuild();
				throw new RuntimeException("Please reload the current page.");
			}
		}
		// staticInvokerSearchedInClassLoader = true;
		if (clz != null && StaticActionInvoker.class.isAssignableFrom(clz)) {
			try {
				Constructor<?> constructor = clz.getConstructor();
				StaticActionInvoker inst = (StaticActionInvoker) constructor.newInstance();
				staticActionInvoker = inst;
			} catch (Exception e) {
				Logger.error(StaticRouterPlugin.DISPATCHER_CLASS, e);
				return;
			}
		}
	}

	// ApplicationClass ac = null;
	private StaticActionInvoker staticActionInvoker;

	// private boolean staticInvokerSearchedInClassLoader = false;

	public void clearStaticActionInvoker() {
		this.staticActionInvoker = null;
		// staticInvokerSearchedInClassLoader = false;
		ApplicationClass ac = getApplicationClass(StaticRouterPlugin.DISPATCHER_CLASS);
		if (ac != null) {
			ac.refresh();
		}
	}

}
