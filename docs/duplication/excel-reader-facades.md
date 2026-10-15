# Excel2007Utils and ExcelReader are parallel facades

`Excel2007Utils` (`:13-31`) and `ExcelReader` (`:21-39`) are the same facade over XSSF and HSSF.

## Done when

One facade uses `Workbook` and `WorkbookFactory` for both formats.
