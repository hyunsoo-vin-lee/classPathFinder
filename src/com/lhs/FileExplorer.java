package com.lhs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class FileExplorer {
	
	public static BufferedWriter bw = null;
	
	public void test() {
		
	}
	
	public void aboutClass(String sFileName, String sFormatName, String qualifiedName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		String sClassName = sFileName.replace("." + sFormatName, "");
		sClassName = sClassName.replaceAll("/", ".");
		String sQualifiedName = getQualifiedName(qualifiedName, sClassName);
		
		System.out.println(sQualifiedName);
		
		try {
			Class clazz = Class.forName(sQualifiedName);
			Annotation[] annotations = clazz.getAnnotations();
			printAnnotations(annotations, "\t", sQualifiedName, null);
			
			Method[] methods = clazz.getMethods();
			for (Method method : methods)
			{
				System.out.println("\t\t" + method.getName());
				annotations = method.getAnnotations();
				printAnnotations(annotations, "\t\t\t", sQualifiedName, method.getName());
			}
		} catch (NoClassDefFoundError error) {
			// TODO Auto-generated catch block
		} catch (ClassFormatError error) {
			// TODO Auto-generated catch block
		} catch (ExceptionInInitializerError error) {
			// TODO Auto-generated catch block
		} catch (UnsatisfiedLinkError error) {
			// TODO Auto-generated catch block
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void explore(File file, String qualifiedName) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		File[] listFiles = file.listFiles();
		
		String sFileName = null;
		String sFormatName = null;
		
		for (File tempFile : listFiles)
		{
			sFileName = tempFile.getName();
			
			if ( tempFile.isDirectory() )
			{
				explore(tempFile, getQualifiedName(qualifiedName, sFileName));
			}
			else
			{
				sFormatName = sFileName.substring(sFileName.lastIndexOf(".") + 1, sFileName.length());
				
				switch (sFormatName) {
				case "class":
					aboutClass(sFileName, sFormatName, qualifiedName);
					break;
				case "jar":
					System.out.println(tempFile);
					JarFile jar = new JarFile(tempFile);
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements())
					{
						JarEntry entry = entries.nextElement();
						sFileName = entry.getName();
						sFormatName = sFileName.substring(sFileName.lastIndexOf(".") + 1, sFileName.length());
						
						if ( "class".equals(sFormatName) )
						{
							aboutClass(sFileName, sFormatName, qualifiedName);
						}
						else
						{
							// do nothing...
						}
					}
					break;
					
				default:
					break;
				}
			}
		}
	}
	
	public void printAnnotations(Annotation[] annos, String tab, String qualifiedName, String methodName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, SecurityException {
		Class<? extends Annotation> annotationType = null;
		StringBuffer sbAnno = new StringBuffer();
		boolean bStart = true;
		for (Annotation anno : annos)
		{
			annotationType = anno.annotationType();
			
			sbAnno.delete(0, sbAnno.length());
			sbAnno.append(qualifiedName);
			sbAnno.append(",");
			if ( methodName == null )
			{
			}
			else
			{
				sbAnno.append(",");
				sbAnno.append(methodName);
				sbAnno.append(",");
			}
			sbAnno.append(annotationType.getName());
			sbAnno.append("(");
			bStart = true;
			
			for (Method method : annotationType.getDeclaredMethods())
			{
				if (bStart)
				{
					bStart = false;
				}
				else
				{
					sbAnno.append("|");
				}
				Object value = method.invoke(anno, (Object[])null);
				sbAnno.append(method.getName());
				sbAnno.append("=\"");
				sbAnno.append(value);
				sbAnno.append("\"");
			}
			
			sbAnno.append(")");
			System.out.println(sbAnno.toString());
			
			if ( methodName == null )
			{
				sbAnno.append(",,");
			}
			else
			{
			}
			sbAnno.append("\n");
			
			bw.write(sbAnno.toString());
		}
	}
	
	public String getQualifiedName(String qualifiedName, String sClassName) {
		if ( qualifiedName != null )
		{
			return qualifiedName + "." + sClassName;
		}
		else
		{
			if ( qualifiedName == null && ("classes".contentEquals(sClassName) || "lib".contentEquals(sClassName)) )
			{
				return null;
			}
			else
			{
				return sClassName;
			}
		}
	}

	public static void main(String[] args) {
		FileExplorer explorer = new FileExplorer();
		
		try {
			bw = new BufferedWriter(new FileWriter(new File("c:\\temp\\annotation.csv")));
			bw.write("Class,Annotation,Method,Annotation");
			
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			Enumeration<URL> systemResources = contextClassLoader.getSystemResources("");
			
			while (systemResources.hasMoreElements())
			{
				URL url = systemResources.nextElement();
//				File fClassPath = new File(url.toURI()); 
				File fClassPath = new File("C:\\Users\\3dsuser\\git\\classPathFinder\\WebContent\\WEB-INF"); 
				System.out.println("CLASSPATH=" + fClassPath.getAbsolutePath());
				explorer.explore(fClassPath, null);
			}
			
			bw.flush();
		} catch(Exception e) {
			
		} finally {
			if (bw != null)
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}
}
