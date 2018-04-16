package org.o7planning.springmvcshoppingcart.config;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class SpringWebAppInitializer {

	/**ServletContext - definiuje zestaw metod używanych przez serwlet do komunikowania się 
	 * ze swoim kontenerem serwletów */
	// @Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		/**Stworzenie kontekstu aplikacji dla aplikacji internetowej przy użyciu klasy javy jako 
		 * danych wejściowych dla definicji komponentów bean, zamiast plików xml. */
		AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
		appContext.register(ApplicationContextConfig.class);

		/**ServletRegistration.Dynamic - interfejs, przez który Servlet zarejestrowany za pomocą jednej 
		 * z metod addServlet w ServletContext może być dalej skonfigurowany. */
		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("SpringDispatcher",
				new DispatcherServlet(appContext));
		/** Ustawia priorytet loadOnStartup na Servlet reprezentowany przez ten dynamiczny 
		 * ServletRegistration.*/
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping("/");
		
		/**ContextLoaderListener tworzy główny kontekst aplikacji WWW dla aplikacji WWW i umieszcza go 
		 * w ServletContext. W tym kontekście można załadować i rozładować spring-managed beans 
		 * w zależności od technologii używanej w warstwie sterownika (Struts lub Spring MVC).*/
		ContextLoaderListener contextLoaderListener = new ContextLoaderListener(appContext);
		servletContext.addListener(contextLoaderListener);

		/**FilterRegistration.Dynamic - Interfejs, w którym filtr zarejestrowany za pomocą jednej 
		 * z metod addFilter w ServletContext może być dalej skonfigurowany. */
		FilterRegistration.Dynamic filterRegistration = servletContext.addFilter("encodingFilter",
				CharacterEncodingFilter.class);
		filterRegistration.setInitParameter("encoding", "UTF-8");
		filterRegistration.setInitParameter("forceEncoding", "true");
		filterRegistration.addMappingForUrlPatterns(null, true, "/*");
	}

}
