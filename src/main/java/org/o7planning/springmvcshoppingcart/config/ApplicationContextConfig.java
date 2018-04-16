package org.o7planning.springmvcshoppingcart.config;

import java.util.Properties;

import javax.sql.DataSource;
 
import org.hibernate.SessionFactory;
import org.o7planning.springmvcshoppingcart.dao.AccountDAO;
import org.o7planning.springmvcshoppingcart.dao.OrderDAO;
import org.o7planning.springmvcshoppingcart.dao.ProductDAO;
import org.o7planning.springmvcshoppingcart.dao.impl.AccountDAOImpl;
import org.o7planning.springmvcshoppingcart.dao.impl.OrderDAOImpl;
import org.o7planning.springmvcshoppingcart.dao.impl.ProductDAOImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@ComponentScan("org.o7planning.springmvcshoppingcart.*")
@EnableTransactionManagement
// Load to Environment.
@PropertySource("classpath:ds-hibernate-cfg.properties")
public class ApplicationContextConfig {

	/** Klasa Environment służy jako właściciel właściwości
	    i przechowuje wszystkie właściwości załadowane przez @PropertySource*/
	@Autowired
	private Environment env;
	
	/** Jest to wdrożenie MessageSource. ResourceBundleMessageSource uzyskuje dostęp do pakietu 
	 *  zasobów dla podanych nazw bazowych.*/
	@Bean
	public ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource rb = new ResourceBundleMessageSource();
		// Load property in message/validator.properties
		rb.setBasenames(new String[] { "messages/validator" });
		return rb;
	}

	/** InternalResourceViewResolver służy do rozpoznawania "widoku zasobów wewnętrznych" 
	 * (w prostej, końcowej wersji strony, jsp lub htmp) na podstawie predefiniowanego wzorca adresu URL. 
	 * Dodatkowo pozwala na dodanie wstępnie zdefiniowanego prefiksu lub sufiksu do nazwy widoku 
	 * (prefiks + nazwa widoku + sufiks) i wygenerowanie końcowego adresu URL strony widoku.*/
	@Bean(name = "viewResolver")
	public InternalResourceViewResolver getViewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/WEB-INF/pages/");
		viewResolver.setSuffix(".jsp");
		return viewResolver;
	}

	/** Udostępnia ustawienia "maxUploadSize", "maxInMemorySize" i "defaultEncoding" jako właściwości 
	 *  komponentu bean (dziedziczone z CommonsFileUploadSupport).*/
	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
		// Set Max Size...
		// commonsMultipartResolver.setMaxUploadSize(...);
		return commonsMultipartResolver;
	}
	/** Prosta implementacja standardowego interfejsu JDBC DataSource, konfigurowanie starego sterownika 
	 * JDBC DriverManager za pomocą właściwości komponentu bean i zwracanie nowego połączenia z każdego 
	 * wywołania getConnection.UWAGA: Ta klasa nie jest rzeczywistą pulą połączeń; w rzeczywistości 
	 * nie łączy połączeń. Służy on jedynie jako prosty zamiennik pełnej puli połączeń, 
	 * implementując ten sam standardowy interfejs, ale tworząc nowe połączenia przy każdym połączeniu.*/
	@Bean(name = "dataSource")
	public DataSource getDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		// See: ds-hibernate-cfg.properties
		dataSource.setDriverClassName(env.getProperty("ds.database-driver"));
		dataSource.setUrl(env.getProperty("ds.url"));
		dataSource.setUsername(env.getProperty("ds.username"));
		dataSource.setPassword(env.getProperty("ds.password"));

		System.out.println("## getDataSource: " + dataSource);

		return dataSource;
	}

	/**Głównym kontraktem jest tworzenie instancji Sesji. Zwykle aplikacja ma jedną instancję 
	 * SessionFactory, a wątki obsługujące żądania klientów uzyskują instancje sesji z tej fabryki.
	 * Wewnętrzny stan SessionFactory jest niezmienny. Po utworzeniu ten stan wewnętrzny jest ustawiony. 
	 * Ten stan wewnętrzny obejmuje wszystkie metadane dotyczące odwzorowania obiektów / relacji. */
	@Autowired
	@Bean(name = "sessionFactory")
	public SessionFactory getSessionFactory(DataSource dataSource) throws Exception {
		Properties properties = new Properties();

		// See: ds-hibernate-cfg.properties
		properties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
		properties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
		properties.put("current_session_context_class", env.getProperty("current_session_context_class"));

		LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();

		// Package contain entity classes
		factoryBean.setPackagesToScan(new String[] { "org.o7planning.springmvcshoppingcart.entity" });
		factoryBean.setDataSource(dataSource);
		factoryBean.setHibernateProperties(properties);
		factoryBean.afterPropertiesSet();
		//
		SessionFactory sf = factoryBean.getObject();
		System.out.println("## getSessionFactory: " + sf);
		return sf;
	}
	
	/**Implementacja PlatformTransactionManager dla pojedynczego Hibernate SessionFactory. 
	 * Przywiązuje sesję hibernacji z określonej fabryki do wątku, potencjalnie pozwalając 
	 * na jedną sesję związaną z wątkami na fabrykę.  */
	@Autowired
	@Bean(name = "transactionManager")
	public HibernateTransactionManager getTransactionManager(SessionFactory sessionFactory) {
		HibernateTransactionManager transactionManager = new HibernateTransactionManager(sessionFactory);

		return transactionManager;
	}

	/**DAO - W aplikacji J2EE w pewnym momencie może zajść potrzeba odczytu / zapisu dane do bazowego 
	 * magazynu trwałości. Aby oddzielić logikę trwałości od logiki biznesowej, możemy użyć wzoru 
	 * projektu DAO. DAO jest prostą klasą języka Java. Klasa DAO zawiera tylko logikę dla dostępu 
	 * do danych / pamięci. Może zawierać dowolną liczbę metod, takich jak zapisywanie, 
	 * aktualizowanie, usuwanie i znajdowanie itp.*/
	@Bean(name = "accountDAO")
	public AccountDAO getApplicantDAO() {
		return new AccountDAOImpl();
	}

	@Bean(name = "productDAO")
	public ProductDAO getProductDAO() {
		return new ProductDAOImpl();
	}
	
	@Bean(name = "orderDAO")
	public OrderDAO getOrderDAO() {
		return new OrderDAOImpl();
	}

	@Bean(name = "accountDAO")
	public AccountDAO getAccountDAO() {
		return new AccountDAOImpl();
	}
	
	/**ApplicationContextConfig odczyta informacje o konfiguracji źródła danych 
	 * i właściwości hibernacji w pliku ds-hibernate-cfg.properties. */

}
