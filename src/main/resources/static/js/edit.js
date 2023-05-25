'use strict';
let saveData = {};
/********************************************************************************************************************************************
Common Func
********************************************************************************************************************************************/
function readURL($input,replaceStr,selector) {
	let reader = new FileReader();
	try{
		reader.onload = function(e) {
	      	$(`${selector}`).attr('src', e.target.result);	
	    };
	    reader.readAsDataURL($input.files[0]);
	    saveData[`${$input.id}`] = $input.files[0]; 		
	}catch(err){
		console.error(err);
		return false;
	}
	return true;
}

function selectorClick(elNum,selector){
	$(selector+elNum).trigger( 'click' );
}


function save(){
	let _templateCode = getTemplateCode();
	
	if(!validCheck(_templateCode)){
		return false;
	}
	 
	
	let formData = new FormData();
		formData.append("templateCode", getTemplateCode());
		for(let key in saveData){
		  formData.append(key, saveData[key]);
		}
		
	if(_templateCode === "2"){
		formData.append("qrcodeUrl", $("#qrcodeUrl").val().trim());
		formData.append("qrCodeLocation",$("#qrCodeLocation option:selected").val());
	}
	
	$.ajax({
	    type : 'post',          
	    url : '/api/v1/stream/save',
	    async : false,
	    enctype:'multipart/form-data',
	    contentType : false,
	    processData : false,
	    dataType:'json',
	    data : formData,
	    success : function(result) { // 결과 성공 콜백함수
	        console.info(result);
	        if(result["status"] === "success"){
				$("#resultVideo").attr('src', `/api/v1/stream/${result['resultFileName']}`);
				alert("동영상 제작 완료");
			}
	    },
	    error : function(request, status, error) { // 결과 에러 콜백함수
	        console.log(error);
	    }
	})
	
	return true;
}

function validCheck(tCode){
	if(tCode === null || tCode === undefined){
		alert("템플릿 코드가 유효하지 않습니다.");
		return false;
	}
	
	if(tCode === "2"){
		if($("#qrcodeUrl").val().trim() === ""){
			alert("qrCdoe URL 을 입력해주세요.");
			$("#qrcodeUrl").focus();
			return false;
		}
	}
	
	if($.isEmptyObject(saveData) ){
		alert("저장할 데이터가 없습니다.");
		return false;
	}
	
	return true;
}

function getTemplateCode(){
	let _templateCode = location.pathname.replace("/pages/templateEdit/","");
	return _templateCode; 
}

function editForm(_flag){
	if(_flag){
		
	}else{
		
	}
}


/********************************************************************************************************************************************
Editor Func
********************************************************************************************************************************************/
