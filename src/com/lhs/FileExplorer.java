package com.lhs;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/rest")
public class FileExplorer {
	
	@GET
	@Path("/test")
	public void test() {
		
	}
	
	public void explore(File file, String qualifiedName) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		File[] listFiles = file.listFiles();
		
		String sFileName = null;
		String sFormatName = null;
		String sClassName = null;
		String sQualifiedName = null;
		Class clazz = null;
		Annotation[] annotations = null;
		Method[] methods = null;
		
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
					sClassName = sFileName.replace("." + sFormatName, "");
					sQualifiedName = getQualifiedName(qualifiedName, sClassName);
					
					System.out.println(sQualifiedName);
					
					clazz = Class.forName(sQualifiedName);
					annotations = clazz.getAnnotations();
					printAnnotations(annotations, "\t");
					
					methods = clazz.getMethods();
					for (Method method : methods)
					{
						System.out.println("\t\t" + method.getName());
						annotations = method.getAnnotations();
						printAnnotations(annotations, "\t\t\t");
					}
					break;
				case "jar":
					
					break;
					
				default:
					break;
				}
			}
		}
	}
	
	public void printAnnotations(Annotation[] annos, String tab) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<? extends Annotation> annotationType = null;
		StringBuffer sbAnno = new StringBuffer();
		boolean bStart = true;
		for (Annotation anno : annos)
		{
			annotationType = anno.annotationType();
			
			sbAnno.delete(0, sbAnno.length());
			sbAnno.append(tab);
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
					sbAnno.append(",");
				}
				Object value = method.invoke(anno, (Object[])null);
				sbAnno.append(method.getName());
				sbAnno.append("=\"");
				sbAnno.append(value);
				sbAnno.append("\"");
			}
			
			sbAnno.append(")");
			System.out.println(sbAnno.toString());
		}
	}
	
	public String getQualifiedName(String qualifiedName, String sClassName) {
		if ( qualifiedName != null )
		{
			return qualifiedName + "." + sClassName;
		}
		else
		{
			return sClassName;
		}
	}

	public static void main(String[] args) throws IOException, URISyntaxException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		FileExplorer explorer = new FileExplorer();
		
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Enumeration<URL> systemResources = contextClassLoader.getSystemResources("");
		
		while (systemResources.hasMoreElements())
		{
			URL url = systemResources.nextElement();
			File fClassPath = new File(url.toURI());
			explorer.explore(fClassPath, null);
		}
	}
}
