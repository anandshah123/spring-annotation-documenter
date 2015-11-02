# spring-annotation-documenter
Documentation of all spring projects annotations. Generates most widely used spring framework annotations with javadocs

## Objective
To prepare mini spring annotation cheat sheet. 
- This should ideally include all annotations defined by various spring projects (springframework, web, data, boot, cloud and many more like this.)
- It should list all annotation along with it's javadocs
- Spring is one of the fastest moving projects. We should be able to keep this upto date.

## How do we do it?
1. It starts with adding starter `pom.xml` from spring boot initializer http://start.spring.io/
2. Take everything. Included all projects `spring-starters`. refer `pom.xml`
3. Scan all annotation types from classpath which has package starting with `org.springframework`
 
  ``` xml
  <dependency>
      <groupId>org.reflections</groupId>
      <artifactId>reflections</artifactId>
      <version>0.9.9</version>
  </dependency>
  ```
  and
  ``` java
  Reflections reflections = new Reflections("org.springframework");
          List<Class<?>> clazzes = new ArrayList<Class<?>>(
                  reflections.getTypesAnnotatedWith(Retention.class, true));
  ```
  
4. Download sources and javadocs using maven 
  ```
  mvn dependency:sources
  mvn dependency:resolve -Dclassifier=javadoc
  ```
5. So we know how maven keeps things in `.m2`. `spring-xxx-version.jar` and `spring-xxx-version-javadoc.jar` are at same location.
Let's find location of jar from where this annotation is coming.

  ``` java
  File jar = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
  ```
  
6. We can similarly reach to `javadoc html` file as well.
  
  ``` java
  String html = readZipFile(jar.getAbsolutePath().replace(".jar", "-javadoc.jar"), clazz.getCanonicalName().replace(".", "/") + ".html");
  ```
  
7. We also know the structure of javadocs and jsoup ;)
  
  ``` java
  Document document = Jsoup.parse(html);
  Elements elements = document.select("div.block");
  elements.first().html(); // Here comes javadoc
  ```
  
8. Write everything to `annotation.html` and decorate it using some [css](https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css) from [Bootstrap](http://getbootstrap.com/)
9. And see what we have got [Ready reference to spring annotations](https://rawgit.com/anandshah123/spring-annotation-documenter/master/annotations.html)

## Future
Just clone it and run `DocumentationGenerator.main()` as many time we need. (Whenever spring community does few more commits)
