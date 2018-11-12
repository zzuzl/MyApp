package cn.zlihj.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class IntEnumTypeHandler<S> extends ConvertableEnumTypeHandler<S, Integer> {

	IntEnumTypeHandler(ConvertableContext<S, Integer> convertableContext) {
		super(convertableContext);
	}

	@Override
	protected void setValue(PreparedStatement ps, int i, Integer value) throws SQLException {
		ps.setInt(i, value);
	}

	@Override
	protected Integer getValue(ResultSet rs, String columnName) throws SQLException {
		return rs.getInt(columnName);
	}

	@Override
	protected Integer getValue(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getInt(columnIndex);
	}

	@Override
	public Integer getValue(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getInt(columnIndex);
	}

}
