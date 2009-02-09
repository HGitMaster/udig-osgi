' Rename files with the specified regexp

If WScript.Arguments.Count <> 2 Then
   s = "regexprename.vbs was called with " & WScript.Arguments.Count _
      & " arguments:" & vbNewLine
   For Each argument In WScript.Arguments
      s = s & vbTab & argument & vbNewLine
   Next ' argument
   s = s & "But it requires exactly 2 arguments:" & vbNewLine _
      & vbTab & "1. A regular expression to match file names" & vbNewLine _
      & vbTab & "2. A rename expression, which may contain $1, $2, etc." _
      & vbNewLine _
      & "See http://winhlp.com/node/276 and regexprename.htm for details." _
      & vbNewLine _
      & "Aborting."
   MsgBox s, vbOkOnly + vbCritical, "Error - Abort"
Else
   Set fs = CreateObject("Scripting.FileSystemObject")
   Set folder = fs.GetFolder(".")
   Set fileCollection = folder.Files
   Set regEx = New RegExp

   regEx.Pattern = WScript.Arguments(0)
   
   ' Rename start
      For Each file In fileCollection
            If file.Name <> WScript.ScriptName Then
            newFilename = regEx.Replace(file.Name, WScript.Arguments(1))
            If newFilename <> file.Name Then fs.MoveFile file, newFilename
         End If
      Next
   ' Rename end
End If
