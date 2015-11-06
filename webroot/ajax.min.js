function createXHR()
{
    var xhr;
    if (window.ActiveXObject)
    {
        try
        {
            xhr = new ActiveXObject("Microsoft.XMLHTTP");
        }
        catch(e)
        {
            alert(e.message);
            xhr = null;
        }
    }
    else
    {
        xhr = new XMLHttpRequest();
    }

    return xhr;
}