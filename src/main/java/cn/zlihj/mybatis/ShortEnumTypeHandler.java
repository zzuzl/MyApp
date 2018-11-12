package cn.zlihj.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class ShortEnumTypeHandler<S> extends ConvertableEnumTypeHandler<S, Short> {

	ShortEnumTypeHandler(ConvertableContext<S, Short> convertableContext) {
		super(convertableContext);
	}

	@Override
	protected void setValue(PreparedStatement ps, int i, Short value) throws SQLException {
		ps.setShort(i, value);
	}

	@Override
	protected Short getValue(ResultSet rs, String columnName) throws SQLException {
		return rs.getShort(columnName);
	}

	@Override
	protected Short getValue(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getShort(columnIndex);
	}

	@Override
	public Short getValue(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getShort(columnIndex);
	}

}
