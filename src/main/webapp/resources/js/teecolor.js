 
 function setSelectionColor() {
     // not working !! 0-11-2023
    console.log("entering setSelectionColor");
   // var theVar = #{courseC.teeStartList(player)};
    // example input = [WHITE / M / 01-18 / 98, BLACK / M / 01-18 / 108, YELLOW / M / 01-18 / 144, BLUE / M / 01-18 / 146, RED / M / 01-18 / 249]
    var array1 = document.getElementById('tees').value;
       console.log("array1 = " + array1);
    array1 = array1.toString().slice(1, -1).split(','); // enlève [ et ] devient String, avec split redevient array !!
       console.log("array1 after slice and split = " + array1);
    var array2=[];
    let i = 0;
    while (i < array1.length) {
        let element = array1[i];
          console.log("element = " + element); // exemple : WHITE / M / 01-18 / 98
        let color = element.substring(0,element.indexOf(" / ")).toLowerCase();
        let v1 = "<span style='color:" + color + ";background:lightgreen'>" + element + "</span>";
        array2.push(v1);
          //console.log("array2 with v1 = " + array2 + "for i = " + i);
         i++;
    } // end while
 console.log("array2 completed with v1 = " + array2);

  document.getElementById('teeStartList').innerHTML = array2.toString();
 // Refresh the page and bypass the cache
// location.reload(true);

} //end 