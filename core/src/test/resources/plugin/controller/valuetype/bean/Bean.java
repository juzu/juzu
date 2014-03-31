package plugin.controller.valuetype.bean;

import java.util.List;

@juzu.Mapped
public class Bean {

  public String s;
  public List<String> list;
  public Integer integer;
  public List<Integer> listInteger;
  public Integer[] integerArray;
  public int integerPrimitive;
  public int[] integerPrimitiveArray;

  String _s;
  List<String> _list;
  Integer _integer;
  List<Integer> _listInteger;
  Integer[] _integerArray;
  int _integerPrimitive;
  int[] _integerPrimitiveArray;

  public String getS2() { return this._s; }
  public void setS2(String value) { this._s = value; }
  public List<String> getList2() { return this._list; }
  public void setList2(List<String> value) { this._list = value; }
  public Integer getInteger2() { return this._integer; }
  public void setInteger2(Integer value) { this._integer = value; }
  public List<Integer> getListInteger2() { return this._listInteger; }
  public void setListInteger2(List<Integer> value) { this._listInteger = value; }
  public Integer[] getIntegerArray2() { return this._integerArray; }
  public void setIntegerArray2(Integer[] value) { this._integerArray = value; }
  public int getIntegerPrimitive2() { return this._integerPrimitive; }
  public void setIntegerPrimitive2(int value) { this._integerPrimitive = value; }
  public int[] getIntegerPrimitiveArray2() { return this._integerPrimitiveArray; }
  public void setIntegerPrimitiveArray2(int[] value) { this._integerPrimitiveArray = value; }

}