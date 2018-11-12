package cn.zlihj.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class ByteEnumTypeHandler<S> extends ConvertableEnumTypeHandler<S, Byte> {

	ByteEnumTypeHandler(ConvertableContext<S, Byte> convertableContext) {
		super(convertableContext);
	}

	@Override
	protected void setValue(PreparedStatement ps, int i, Byte value) throws SQLException {
		ps.setByte(i, value);
	}

	@Override
	protected Byte getValue(ResultSet rs, String columnName) throws SQLException {
		return rs.getByte(columnName);
	}

	@Override
	protected Byte getValue(CallableStatement cs, int columnIndex) throws SQLException {
		return cs.getByte(columnIndex);
	}
	
	@Override
	public Byte getValue(ResultSet rs, int columnIndex) throws SQLException {
		return rs.getByte(columnIndex);
	}

}
