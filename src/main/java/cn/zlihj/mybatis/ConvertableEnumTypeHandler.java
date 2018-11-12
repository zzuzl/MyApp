package cn.zlihj.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

abstract class ConvertableEnumTypeHandler<S, T> extends BaseTypeHandler {
	private final ConvertableContext<S, T> convertableContext;

	ConvertableEnumTypeHandler(ConvertableContext<S, T> convertableContext) {
		this.convertableContext = convertableContext;
	}

	protected abstract void setValue(PreparedStatement ps, int i, T value) throws SQLException;

	protected abstract T getValue(ResultSet rs, String columnName) throws SQLException;

	protected abstract T getValue(CallableStatement cs, int columnIndex) throws SQLException;

	protected abstract T getValue(ResultSet rs, int columnIndex) throws SQLException;

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
		@SuppressWarnings("unchecked")
		S source = (S) parameter;
		T v;
		try {
			v = convertableContext.value(source);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (v == null) {
			ps.setNull(i, jdbcType.TYPE_CODE);
		} else {
			setValue(ps, i, v);
		}
	}

	@Override
	public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
		T v = getValue(rs, columnName);
		if (rs.wasNull()) {
			return null;
		}
		try {
			return convertableContext.of(v);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		T v = getValue(rs, columnIndex);
		if (rs.wasNull()) {
			return null;
		}
		try {
			return convertableContext.of(v);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		T v = getValue(cs, columnIndex);
		if (cs.wasNull()) {
			return null;
		}
		try {
			return convertableContext.of(v);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
