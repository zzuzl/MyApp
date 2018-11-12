package cn.zlihj.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class StringEnumTypeHandler<S> extends ConvertableEnumTypeHandler<S, String> {

	StringEnumTypeHandler(ConvertableContext<S, String> convertableContext) {
		super(convertableContext);
	}

	@Override
	protected void setValue(PreparedStatement ps, int i, String value) throws SQLException {
		ps.setString(i, value);
	}

	@Override
	protected String getValue(ResultSet rs, String columnName) throws SQLException {
		return rs.getString(columnName);
	}

	@Override
	protected String getValue(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getString(columnIndex);
	}
	
	@Override
	public String getValue(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getString(columnIndex);
	}

}
