In order to run the search engine:
1. Install tomcat and set the environment variables "as we're using servlets"
2. Edit Configuration and choose Tomcat as the Application server (Tomcat 10.1.20 is preferred)
3. If it did't run properly make sure that Deployment link is :
/search_engine_app_war_exploded 
4. In case it did't recogonize the extra libraries make sure you include both 
	-jsp-api.jar
	-servlet-api.jar

