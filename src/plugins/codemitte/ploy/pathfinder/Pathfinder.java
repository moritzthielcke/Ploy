/**
 *
 * Pathfinder
 * 
 * A annotation based router plugin for Play
 * 
 *  ( based the "Router annotations"-Plugin: https://github.com/digiPlant/play-router-annotations )
 * 
 * @author Moritz.thielcke  at googlemail.com 
 * 
 */
package plugins.codemitte.ploy.pathfinder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import play.Logger;
import play.PlayPlugin;
import play.Play;
import play.classloading.ApplicationClasses;
import play.mvc.Router;
import play.utils.Java;

/**
 *
 * Pathfinder - Plugin  Implementation
 * 
 */
public class Pathfinder extends PlayPlugin{
    
	@Override
	public void onApplicationStart() {
		scanControllers();
	}

        
	@Override
	public List<ApplicationClasses.ApplicationClass> onClassesChange(List<ApplicationClasses.ApplicationClass> modified) {
		scanControllers();
		return super.onClassesChange(modified);
	}

        
	protected void scanControllers() {
            Logger.debug("Pathfinder: scanning controller for resources ");
            List<Class> controllerClasses = getControllerClasses();
            for(Class clazz : controllerClasses){
                Resource a  = (Resource) clazz.getAnnotation(Resource.class);
                if(a!=null)
                    bindClass(a.value(), clazz);                    
            }
            return;
        }
        

        protected void bindClass(String path, Class clazz){
            String controllerPath = clazz.getName().replaceFirst("controllers\\.", "");
            Logger.debug(" Pathfinder -> "+path+" => "+controllerPath);
            
            //find subresources:
            Bindings bindings = (Bindings) clazz.getAnnotation(Bindings.class);
            if(bindings!=null && bindings.value().length > 0 ){
                for(int i=0;i<bindings.value().length;i++)
                    bindClass(path+bindings.value()[i].path(),  bindings.value()[i].controller());        
            }
            Bind bind = (Bind) clazz.getAnnotation(Bind.class);
            if(bind!=null){
                bindClass(path+bind.path(), bind.controller());
            }
            
            //bind http methods to router:
            List<Method> gets = Java.findAllAnnotatedMethods(clazz, Get.class);   
            for (Method get : gets) {
                    Get annotation = get.getAnnotation(Get.class);
                    if (annotation != null) 
                        bind(annotation.priority(), "GET", path+annotation.value(), controllerPath+"."+get.getName(),  getFormat(annotation.format()), annotation.accept() );                
            }   
         
            List<Method> posts = Java.findAllAnnotatedMethods(clazz, Post.class);
            for (Method post : posts) {
                    Post annotation = post.getAnnotation(Post.class);
                    if (annotation != null)
                        bind(annotation.priority(),"POST", path+annotation.value(), controllerPath+"."+post.getName(),  getFormat(annotation.format()), annotation.accept() );                       
            }
            
            List<Method> puts = Java.findAllAnnotatedMethods(clazz, Put.class);
            for (Method put : puts) {
                    Put annotation = put.getAnnotation(Put.class);
                    if (annotation != null) 
                        bind(annotation.priority(), "PUT", path+annotation.value(), controllerPath+"."+ put.getName(), getFormat(annotation.format()), annotation.accept());   
            }
            
            List<Method> deletes = Java.findAllAnnotatedMethods(clazz, Delete.class);
            for (Method delete : deletes) {
                    Delete annotation = delete.getAnnotation(Delete.class);
                    if (annotation != null) 
                        bind(annotation.priority(), "DELETE",  path+annotation.value(), controllerPath+"."+ delete.getName(), getFormat(annotation.format()), annotation.accept());
            }
            
            List<Method> heads = Java.findAllAnnotatedMethods(clazz, Head.class);
            for (Method head : heads) {
                    Head annotation = head.getAnnotation(Head.class);
                    if (annotation != null) 
                            bind(annotation.priority(), "HEAD", path+annotation.value(), controllerPath+"."+ head.getName(), getFormat(annotation.format()), annotation.accept());
            }

            List<Method> webSockets = Java.findAllAnnotatedMethods(clazz, WS.class);
            for (Method ws : webSockets) {
                    WS annotation = ws.getAnnotation(WS.class);
                    if (annotation != null) 
                            bind(annotation.priority(), "WS", path+annotation.value(), controllerPath+"."+ ws.getName(), getFormat(annotation.format()), annotation.accept());
            }
                
            List<Method> list = Java.findAllAnnotatedMethods(clazz, Any.class);
            for (Method any : list) {
                    Any annotation = any.getAnnotation(Any.class);
                    if (annotation != null) {
                        bind(annotation.priority(), "*", path+annotation.value(), controllerPath+"."+ any.getName(), getFormat(annotation.format()), annotation.accept());
                    }
            }   
            
        }
        
        
        public static void bind(int prio,  String httpMethod, String path, String methodName, String format, String accept){
            path = path.replaceAll("//", "/");
            Logger.debug( "  Pathfinder binding  "+httpMethod+" to function: "+path+" --> "+methodName);
            //since a resource should always get bindet to a unique path, we just try to use priority 1 as default
            if(prio < 0)
                prio = 1;
            Router.addRoute(prio, httpMethod, path, methodName, format, accept);
        }
        

	public List<Class> getControllerClasses() {
            List<Class> returnValues = new ArrayList<Class>();
            List<ApplicationClasses.ApplicationClass> classes = Play.classes.all();
            for (ApplicationClasses.ApplicationClass clazz : classes) {
                    if (clazz.name.startsWith("controllers.")) {
                            if (clazz.javaClass != null && !clazz.javaClass.isInterface() && !clazz.javaClass.isAnnotation()) {
                                    returnValues.add(clazz.javaClass);
                            }
                    }
            }
            return returnValues;
	}
        

	private String getFormat(String format) {
		if(format == null || format.length() < 1) {
			return null;
		}
		return "(format:'" + format + "')";
	}
 
    
}
