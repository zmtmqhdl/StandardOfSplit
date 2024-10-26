import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.standardofsplit.View.Components.Basic_Button
import com.example.standardofsplit.View.Components.Circle_Button
import com.example.standardofsplit.ViewModel.Start
import com.example.standardofsplit.ui.theme.StandardOfSplitTheme

@Composable
fun StartScreen(
    start: Start,
    intentToReceiptActivity: () -> Unit
) {
    val personcount by start.personCount.observeAsState(2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .offset(y = 200.dp),  // 전체 Column을 위로 약간 이동
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Circle_Button(
                content = "-",
                onClick = { start.decrement() }  // 감소 버튼 클릭 시
            )
            Text(
                text = "$personcount",  // 숫자를 표시
                modifier = Modifier.padding(horizontal = 40.dp),
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold
            )
            Circle_Button(
                content = "+",
                onClick = { start.increment() }  // 증가 버튼 클릭 시
            )
        }

        Spacer(modifier = Modifier.height(20.dp))  // Row와 BTN_Basic 사이에 간격 추가

        Basic_Button(
            content = "정산하기",
            onClick = {
                intentToReceiptActivity() // ReceiptActivity로 이동
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartPreview() {
    val dummyStart = Start()
    StandardOfSplitTheme {
        StartScreen(
            start = dummyStart,
            intentToReceiptActivity = {}
        )
    }
}