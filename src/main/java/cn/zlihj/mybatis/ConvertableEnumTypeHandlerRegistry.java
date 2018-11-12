package cn.zlihj.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConvertableEnumTypeHandlerRegistry implements BeanFactoryPostProcessor, InitializingBean {

	private String sqlSessionFactoryBeanName;
	private String basePackages;

	public void setSqlSessionFactoryBeanName(String sqlSessionFactoryBeanName) {
		this.sqlSessionFactoryBeanName = sqlSessionFactoryBeanName;
	}

	public void setBasePackages(String basePackages) {
		this.basePackages = basePackages;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(sqlSessionFactoryBeanName, "Property 'sqlSessionFactoryBeanName' is required");
		Assert.notNull(basePackages, "Property 'basePackages' is required");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		SqlSessionFactory ssf = beanFactory.getBean(sqlSessionFactoryBeanName, SqlSessionFactory.class);
		TypeHandlerRegistry registry = ssf.getConfiguration().getTypeHandlerRegistry();
		List<Class<?>> convertableClasses = provideConvertableClasses();

		for (Class<?> convertableClasse : convertableClasses) {
			ConvertableContext<?, ?> convertableContext = ConvertableContext.build(convertableClasse);
			Class<?> valueType = convertableContext.getValueType();
			Class<?> sourceType = convertableContext.getSourceType();

			ConvertableEnumTypeHandler<?, ?> handler;
			if (valueType == Byte.class || valueType == byte.class) {
				handler = new ByteEnumTypeHandler(convertableContext);
			} else if (valueType == Short.class || valueType == short.class) {
				handler = new ShortEnumTypeHandler(convertableContext);
			} else if (valueType == Integer.class || valueType == int.class) {
				handler = new IntEnumTypeHandler(convertableContext);
			} else if (valueType == String.class) {
				handler = new StringEnumTypeHandler(convertableContext);
			} else {
				throw new RuntimeException("Convertable enum["+convertableClasse.getClass().getName()+"]'s value type should be byte/short/int.");
			}

			registry.register(sourceType, handler);
		}

	}

	private List<Class<?>> provideConvertableClasses() {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Convertable.class));
		provider.addIncludeFilter(new AssignableTypeFilter(Enum.class));

		String[] packages = StringUtils.tokenizeToStringArray(basePackages, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
		for (String packageName : packages) {
			Set<BeanDefinition> components = provider.findCandidateComponents(packageName);
			for (BeanDefinition component : components) {
				try {
					Class<?> cls = Class.forName(component.getBeanClassName());
					classes.add(cls);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return classes;
	}

}
